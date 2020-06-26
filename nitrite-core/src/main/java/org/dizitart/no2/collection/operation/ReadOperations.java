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
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.IndexAwareFilter;
import org.dizitart.no2.filters.LogicalFilter;
import org.dizitart.no2.filters.NitriteFilter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;

import java.util.List;

/**
 * @author Anindya Chatterjee
 */
class ReadOperations {
    private final String collectionName;
    private final NitriteConfig nitriteConfig;
    private final NitriteMap<NitriteId, Document> nitriteMap;

    ReadOperations(String collectionName,
                   NitriteConfig nitriteConfig,
                   NitriteMap<NitriteId, Document> nitriteMap) {
        this.nitriteMap = nitriteMap;
        this.nitriteConfig = nitriteConfig;
        this.collectionName = collectionName;
    }

    public DocumentCursor find() {
        ReadableStream<NitriteId> readableStream = nitriteMap.keySet();
        return new DocumentCursorImpl(readableStream, nitriteMap);
    }

    public DocumentCursor find(Filter filter) {
        if (filter == null || filter == Filter.ALL) {
            return find();
        }

        prepareFilter(filter);
        ReadableStream<KeyValuePair<NitriteId, Document>> readableStream = nitriteMap.entries();
        FilteredReadableStream filteredReadableStream = new FilteredReadableStream(readableStream, filter);

        return new DocumentCursorImpl(filteredReadableStream, nitriteMap);
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

        IndexCatalog indexCatalog = nitriteConfig.getNitriteStore().getIndexCatalog();
        IndexEntry indexEntry = indexCatalog.findIndexEntry(collectionName, field);
        if (indexEntry != null) {
            String indexType = indexEntry.getIndexType();

            Indexer indexer = nitriteConfig.findIndexer(indexType);
            if (indexer != null) {
                indexAwareFilter.setIsFieldIndexed(true);
                indexAwareFilter.setIndexer(indexer);
                indexAwareFilter.cacheIndexedIds();
            }
        }
    }
}
