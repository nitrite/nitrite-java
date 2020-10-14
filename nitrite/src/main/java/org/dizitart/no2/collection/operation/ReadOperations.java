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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.filters.*;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;

import static org.dizitart.no2.common.Constants.DOC_ID;

/**
 * @author Anindya Chatterjee
 */
class ReadOperations {
    private final String collectionName;
    private final NitriteConfig nitriteConfig;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final DocumentIndexWriter documentIndexWriter;

    ReadOperations(String collectionName,
                   NitriteConfig nitriteConfig,
                   NitriteMap<NitriteId, Document> nitriteMap,
                   DocumentIndexWriter documentIndexWriter) {
        this.nitriteMap = nitriteMap;
        this.nitriteConfig = nitriteConfig;
        this.collectionName = collectionName;
        this.documentIndexWriter = documentIndexWriter;
    }

    public DocumentCursor find(FindOptions findOptions) {
        RecordStream<Pair<NitriteId, Document>> recordStream = findSuitableStream(null, findOptions);
        return new DocumentCursorImpl(recordStream);
    }

    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        if (filter == null || filter == Filter.ALL) {
            return find(findOptions);
        }

        // get all indices for this collection
        // find the suitable index (prefix included)
        // find the index map
        // supply indexmap, collectionmap to filter


        prepareFilter(filter);

        RecordStream<Pair<NitriteId, Document>> recordStream = findSuitableStream(filter, findOptions);
        return new DocumentCursorImpl(recordStream);
    }

    Document getById(NitriteId nitriteId) {
        return nitriteMap.get(nitriteId);
    }

    private void prepareFilter(Filter filter) {
        if (filter instanceof NitriteFilter) {
            NitriteFilter nitriteFilter = (NitriteFilter) filter;
            prepareNitriteFilter(nitriteFilter);

            if (filter instanceof IndexAwareFilter) {
                IndexAwareFilter indexAwareFilter = (IndexAwareFilter) filter;
                prepareIndexedFilter(indexAwareFilter);
            }

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

    private void prepareIndexedFilter(IndexAwareFilter indexAwareFilter) {
        String field = indexAwareFilter.getField();

        IndexDescriptor indexDescriptor = documentIndexWriter.findIndexDescriptor(field);
        if (indexDescriptor != null) {
            String indexType = indexDescriptor.getIndexType();
            NitriteIndexer nitriteIndexer = documentIndexWriter.findIndexer(indexType);
            if (nitriteIndexer != null) {
                indexAwareFilter.setNitriteIndexer(nitriteIndexer);
                indexAwareFilter.setIsFieldIndexed(true);
            }
        } else {
            if (indexAwareFilter.getField().equals(DOC_ID)) {
                // default _id index
                indexAwareFilter.setOnIdField(true);
            }
        }
    }

    private RecordStream<Pair<NitriteId, Document>> findSuitableStream(Filter filter, FindOptions findOptions) {
        if (filter instanceof AndFilter) {
            AndFilter andFilter = (AndFilter) filter;
            Filter lhs = andFilter.getLhs();
            Filter rhs = andFilter.getRhs();

            // TODO: check if compound index is supported

            if (lhs instanceof IndexAwareFilter && ((IndexAwareFilter) lhs).getIsFieldIndexed()) {
                // Indexed AND Filter => IndexScan (LHS) -> Filter (RHS)

                return getFilteredStream(((IndexAwareFilter) lhs), rhs);
            } else if (rhs instanceof IndexAwareFilter && ((IndexAwareFilter) rhs).getIsFieldIndexed()) {
                // Non-Indexed AND Indexed => IndexScan (RHS) -> Filter (LHS)

                return getFilteredStream(((IndexAwareFilter) rhs), lhs);
            } else {
                // Non-Indexed AND Filter => CollectionScan

                RecordStream<Pair<NitriteId, Document>> recordStream = nitriteMap.entries();
                return new FilteredRecordStream(recordStream, filter);
            }
        } else if (filter instanceof OrFilter) {
            OrFilter orFilter = (OrFilter) filter;
            Filter lhs = orFilter.getLhs();
            Filter rhs = orFilter.getRhs();

            if (lhs instanceof IndexAwareFilter && ((IndexAwareFilter) lhs).getIsFieldIndexed()
                && rhs instanceof IndexAwareFilter && ((IndexAwareFilter) rhs).getIsFieldIndexed()) {
                // Indexed OR Indexed => IndexScan (LHS) Union IndexScan (RHS)

                final RecordStream<Pair<NitriteId, Document>> lhsStream
                    = getIndexedStream(((IndexAwareFilter) lhs));
                final RecordStream<Pair<NitriteId, Document>> rhsStream
                    = getIndexedStream(((IndexAwareFilter) rhs));

                return getUnionStream(lhsStream, rhsStream);
            } else {
                // Non-Indexed OR Filter => CollectionScan
                // Indexed OR Non-Indexed => CollectionScan

                RecordStream<Pair<NitriteId, Document>> recordStream = nitriteMap.entries();
                return new FilteredRecordStream(recordStream, filter);
            }
        } else if (filter instanceof IndexAwareFilter && ((IndexAwareFilter) filter).getIsFieldIndexed()) {
            // Indexed => IndexScan

            IndexAwareFilter indexAwareFilter = (IndexAwareFilter) filter;
            return getIndexedStream(indexAwareFilter);
        } else {
            // Non-Indexed => CollectionScan

            RecordStream<Pair<NitriteId, Document>> recordStream = nitriteMap.entries();
            return new FilteredRecordStream(recordStream, filter);
        }
    }

    private RecordStream<Pair<NitriteId, Document>> getFilteredStream(IndexAwareFilter indexAwareFilter,
                                                                      Filter rest) {
        RecordStream<Pair<NitriteId, Document>> indexStream = getIndexedStream(indexAwareFilter);
        if (rest != null) {
            return new FilteredRecordStream(indexStream, rest);
        } else {
            return indexStream;
        }
    }

    private RecordStream<Pair<NitriteId, Document>> getUnionStream(
        RecordStream<Pair<NitriteId, Document>> lhsStream,
        RecordStream<Pair<NitriteId, Document>> rhsStream) {

        return RecordStream.fromIterable(() -> new UnionStreamIterator(lhsStream, rhsStream));
    }

    private RecordStream<Pair<NitriteId, Document>> getIndexedStream(IndexAwareFilter indexAwareFilter) {
        // TODO: check if prefix of compound index is possible

        return new IndexedStream(indexAwareFilter, nitriteMap);
    }
}
