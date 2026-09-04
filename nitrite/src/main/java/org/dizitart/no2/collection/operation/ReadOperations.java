/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.streams.*;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.*;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.store.NitriteMap;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import static org.dizitart.no2.common.tuples.Pair.pair;

/**
 * @author Anindya Chatterjee
 * @since 1.0
 */
class ReadOperations {
    private final String collectionName;
    private final NitriteConfig nitriteConfig;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final FindOptimizer findOptimizer;
    private final IndexOperations indexOperations;
    private final ProcessorChain processorChain;

    ReadOperations(String collectionName,
                   IndexOperations indexOperations,
                   NitriteConfig nitriteConfig,
                   NitriteMap<NitriteId, Document> nitriteMap,
                   ProcessorChain processorChain) {
        this.nitriteMap = nitriteMap;
        this.nitriteConfig = nitriteConfig;
        this.collectionName = collectionName;
        this.indexOperations = indexOperations;
        this.findOptimizer = new FindOptimizer();
        this.processorChain = processorChain;
    }

    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        if (filter == null) {
            filter = Filter.ALL;
        }

        prepareFilter(filter);
        Collection<IndexDescriptor> indexDescriptors = indexOperations.listIndexes();
        FindPlan findPlan = findOptimizer.optimize(filter, findOptions, indexDescriptors);
        return createCursor(findPlan);
    }

    Document getById(NitriteId nitriteId) {
        Document document = nitriteMap.get(nitriteId);
        if (processorChain != null) {
            document = processorChain.processAfterRead(document);
        }
        return document;
    }

    private void prepareFilter(Filter filter) {
        if (filter instanceof NitriteFilter) {
            NitriteFilter nitriteFilter = (NitriteFilter) filter;
            prepareNitriteFilter(nitriteFilter);

            if (filter instanceof LogicalFilter) {
                LogicalFilter logicalFilter = (LogicalFilter) filter;
                prepareLogicalFilter(logicalFilter);
            }
        }
    }

    private void prepareNitriteFilter(NitriteFilter nitriteFilter) {
        nitriteFilter.setNitriteConfig(nitriteConfig);
        nitriteFilter.setCollectionName(collectionName);
    }

    private void prepareLogicalFilter(LogicalFilter logicalFilter) {
        List<Filter> filters = logicalFilter.getFilters();
        for (Filter filter : filters) {
            if (filter instanceof NitriteFilter) {
                NitriteFilter nitriteFilter = (NitriteFilter) filter;
                nitriteFilter.setObjectFilter(logicalFilter.getObjectFilter());
            }
            prepareFilter(filter);
        }
    }

    private DocumentCursor createCursor(FindPlan findPlan) {
        // -1 means "not an index scan"; the index branch records the exact id-set size here.
        long[] indexedIdCount = { -1 };
        RecordStream<Pair<NitriteId, Document>> recordStream = findSuitableStream(findPlan, indexedIdCount);
        DocumentStream cursor = new DocumentStream(recordStream, processorChain);
        cursor.setFindPlan(findPlan);
        cursor.setCoveredCount(computeCoveredCount(findPlan, indexedIdCount[0]));
        return cursor;
    }

    /**
     * Returns the exact match count when the query is fully answered without fetching documents,
     * or {@code null} when the cursor must be drained to count. The count is exact only when
     * nothing downstream drops or changes cardinality (a post-filter, skip, or limit); sort does
     * not change the count, and an OR-union needs de-duplication so its count cannot be derived.
     */
    private Long computeCoveredCount(FindPlan findPlan, long indexedIdCount) {
        if (!findPlan.getSubPlans().isEmpty()
            || findPlan.getCollectionScanFilter() != null
            || findPlan.getSkip() != null
            || findPlan.getLimit() != null
            || findPlan.getByIdFilter() != null) {
            return null;
        }
        if (findPlan.getIndexDescriptor() != null) {
            // the index supplied the exact matching id set
            return indexedIdCount >= 0 ? indexedIdCount : null;
        }
        // pure full scan over the whole collection
        return nitriteMap.size();
    }

    /**
     * Orders the collection from an index on the sort field, so only the documents actually
     * returned are fetched.
     * <p>
     * A blocking sort has to deserialize every stored document just to read one field, which
     * is why {@code orderBy(field).limit(20)} used to cost what draining the whole collection
     * costs. An index on that field already holds the key for every document, so the ordering
     * can be decided without touching a document.
     *
     * @return the ordered stream, or {@code null} when the sort cannot be answered this way -
     * the index is not a faithful stand-in for the collection (a multi-valued field is indexed
     * once per element) and the caller must fall back to the blocking sort.
     */
    // ponytail: reads the whole index (one small entry per document) rather than only the
    // skip+limit entries the page needs, because the faithfulness check needs the total.
    // That trades a document decode per row for an index-entry read per row: a large win
    // when documents are big (37ms -> 0.9ms over 2000 rows carrying a 150-element array),
    // a few hundred microseconds worse when they are tiny. Walking the index lazily in key
    // order would make it O(limit) and remove the loss, but needs a way to know the index
    // covers the collection without reading all of it.
    private RecordStream<Pair<NitriteId, Document>> indexSortedStream(FindPlan findPlan) {
        IndexDescriptor descriptor = findPlan.getSortIndexDescriptor();
        if (descriptor == null) return null;

        NitriteIndexer indexer = nitriteConfig.findIndexer(descriptor.getIndexType());
        List<Pair<DBValue, NitriteId>> sortKeys = indexer.readSortKeys(descriptor, nitriteConfig, nitriteMap.size());
        if (sortKeys == null) return null;

        // the hint is only ever set for a single-field sort, so this is that field
        SortOrder sortOrder = findPlan.getBlockingSortOrder().get(0).getSecond();
        Collator collator = findPlan.getCollator();

        // same comparator and same stability as the blocking sort, so an indexed and an
        // unindexed collection return the same rows in the same order
        sortKeys.sort((a, b) -> {
            // unwrap, or the collator would never see a String and DBNull would not read as null
            int result = DocumentSorter.compareValues(indexedValue(a.getFirst()), indexedValue(b.getFirst()), collator);
            return sortOrder == SortOrder.Descending ? -result : result;
        });

        LinkedHashSet<NitriteId> nitriteIds = new LinkedHashSet<>(sortKeys.size());
        for (Pair<DBValue, NitriteId> sortKey : sortKeys) {
            nitriteIds.add(sortKey.getSecond());
        }
        return new IndexedStream(nitriteIds, nitriteMap);
    }

    private static Object indexedValue(DBValue dbValue) {
        return dbValue == null || dbValue instanceof DBNull ? null : dbValue.getValue();
    }

    private RecordStream<Pair<NitriteId, Document>> findSuitableStream(FindPlan findPlan, long[] indexedIdCount) {
        RecordStream<Pair<NitriteId, Document>> rawStream;
        RecordStream<Pair<NitriteId, Document>> indexSortedStream = null;

        if (!findPlan.getSubPlans().isEmpty()) {
            // or filters get all sub stream by finding suitable stream of all sub plans
            List<RecordStream<Pair<NitriteId, Document>>> subStreams = new ArrayList<>();
            for (FindPlan subPlan : findPlan.getSubPlans()) {
                // a sub-plan's own id count cannot answer the union's count (dedup), so discard it
                RecordStream<Pair<NitriteId, Document>> suitableStream = findSuitableStream(subPlan, new long[]{ -1 });
                subStreams.add(suitableStream);
            }

            // concat all suitable stream of all sub plans
            rawStream = new ConcatStream(subStreams);

            // Always apply distinct stream for OR filters to avoid duplicates
            // when the same document matches multiple sub-plans (different indexes)
            rawStream = new DistinctStream(rawStream);
        } else {
            // and or single filter
            if (findPlan.getByIdFilter() != null) {
                FieldBasedFilter byIdFilter = findPlan.getByIdFilter();
                Object idValue = byIdFilter.getValue();
                // the search term may be any numeric or String representation of an id,
                // e.g. a String for databases written before 4.4 (gh-1263)
                NitriteId nitriteId = idValue instanceof Long
                    ? NitriteId.createId((long) idValue)
                    : NitriteId.createId(String.valueOf(idValue));
                // one lookup: a document removed between containsKey and get would be a null row
                Document document = nitriteMap.get(nitriteId);
                rawStream = document == null
                    ? RecordStream.empty()
                    : RecordStream.single(pair(nitriteId, document));
            } else {
                IndexDescriptor indexDescriptor = findPlan.getIndexDescriptor();
                if (indexDescriptor != null) {
                    // get optimized filter
                    NitriteIndexer indexer = nitriteConfig.findIndexer(indexDescriptor.getIndexType());
                    LinkedHashSet<NitriteId> nitriteIds = indexer.findByFilter(findPlan, nitriteConfig);

                    // the index supplied the exact matching id set; record its size so a size()
                    // with no row-dropping step downstream can answer from it without fetching
                    indexedIdCount[0] = nitriteIds.size();

                    // create indexed stream from optimized filter
                    rawStream = new IndexedStream(nitriteIds, nitriteMap);
                } else {
                    indexSortedStream = indexSortedStream(findPlan);
                    rawStream = indexSortedStream != null ? indexSortedStream : nitriteMap.entries();
                }
            }

            if (findPlan.getCollectionScanFilter() != null) {
                rawStream = new FilteredStream(rawStream, findPlan.getCollectionScanFilter());
            }
        }

        // sort and bound stage
        if (rawStream != null) {
            // the blocking sort still runs whenever the ordered ids were not used - either no
            // index could answer the sort, or the one that could turned out not to cover the
            // collection faithfully
            if (indexSortedStream == null
                && findPlan.getBlockingSortOrder() != null && !findPlan.getBlockingSortOrder().isEmpty()) {
                rawStream = new SortedDocumentStream(findPlan, rawStream);
            }

            if (findPlan.getLimit() != null || findPlan.getSkip() != null) {
                long limit = findPlan.getLimit() == null ? Long.MAX_VALUE : findPlan.getLimit();
                long skip = findPlan.getSkip() == null ? 0 : findPlan.getSkip();
                rawStream = new BoundedStream<>(skip, limit, rawStream);
            }
        }

        return rawStream;
    }
}
