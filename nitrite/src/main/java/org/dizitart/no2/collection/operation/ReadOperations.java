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
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.streams.*;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.*;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.store.NitriteMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import static org.dizitart.no2.common.tuples.Pair.pair;

/**
 * @author Anindya Chatterjee
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
        RecordStream<Pair<NitriteId, Document>> recordStream = findSuitableStream(findPlan);
        DocumentStream cursor = new DocumentStream(recordStream, processorChain);
        cursor.setFindPlan(findPlan);
        return cursor;
    }

    private RecordStream<Pair<NitriteId, Document>> findSuitableStream(FindPlan findPlan) {
        RecordStream<Pair<NitriteId, Document>> rawStream;

        if (!findPlan.getSubPlans().isEmpty()) {
            // or filters get all sub stream by finding suitable stream of all sub plans
            List<RecordStream<Pair<NitriteId, Document>>> subStreams = new ArrayList<>();
            for (FindPlan subPlan : findPlan.getSubPlans()) {
                RecordStream<Pair<NitriteId, Document>> suitableStream = findSuitableStream(subPlan);
                subStreams.add(suitableStream);
            }

            // concat all suitable stream of all sub plans
            rawStream = new ConcatStream(subStreams);

            if (findPlan.isDistinct()) {
                // return only distinct items
                rawStream = new DistinctStream(rawStream);
            }
        } else {
            // and or single filter
            if (findPlan.getByIdFilter() != null) {
                FieldBasedFilter byIdFilter = findPlan.getByIdFilter();
                NitriteId nitriteId = NitriteId.createId((String) byIdFilter.getValue());
                rawStream = RecordStream.single(pair(nitriteId, getById(nitriteId)));
            } else {
                IndexDescriptor indexDescriptor = findPlan.getIndexDescriptor();
                if (indexDescriptor != null) {
                    // get optimized filter
                    NitriteIndexer indexer = nitriteConfig.findIndexer(indexDescriptor.getIndexType());
                    LinkedHashSet<NitriteId> nitriteIds = indexer.findByFilter(findPlan, nitriteConfig);

                    // create indexed stream from optimized filter
                    rawStream = new IndexedStream(nitriteIds, nitriteMap);
                } else {
                    rawStream = nitriteMap.entries();
                }
            }

            if (findPlan.getCollectionScanFilter() != null) {
                rawStream = new FilteredStream(rawStream, findPlan.getCollectionScanFilter());
            }
        }

        // sort and bound stage
        if (rawStream != null) {
            if (findPlan.getBlockingSortOrder() != null && !findPlan.getBlockingSortOrder().isEmpty()) {
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
