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
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.util.ValidationUtils.validateDocumentIndexField;

/**
 * @author Anindya Chatterjee
 */
class IndexOperations implements AutoCloseable {
    private final NitriteConfig nitriteConfig;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private String collectionName;
    private IndexCatalog indexCatalog;
    private Map<String, AtomicBoolean> indexBuildRegistry;
    private ExecutorService rebuildExecutor;

    IndexOperations(NitriteConfig nitriteConfig, NitriteMap<NitriteId, Document> nitriteMap,
                    EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus) {
        this.nitriteConfig = nitriteConfig;
        this.nitriteMap = nitriteMap;
        this.eventBus = eventBus;
        init();
    }

    void ensureIndex(String field, String indexType, boolean isAsync) {
        IndexEntry indexEntry;
        if (!hasIndexEntry(field)) {
            // if no index create index
            indexEntry = indexCatalog.createIndexEntry(collectionName, field, indexType);
        } else {
            // if index already there throw
            throw new IndexingException("index already exists on " + field);
        }

        rebuildIndex(indexEntry, isAsync);
    }

    void writeIndex(Document document, NitriteId nitriteId) {
        Collection<IndexEntry> indexEntries = listIndexes();
        if (indexEntries != null) {
            for (IndexEntry indexEntry : indexEntries) {
                String field = indexEntry.getField();
                String indexType = indexEntry.getIndexType();
                Indexer indexer = findIndexer(indexType);

                writeIndexEntry(field, document, nitriteId, indexer, indexEntry);
            }
        }
    }

    void removeIndex(Document document, NitriteId nitriteId) {
        Collection<IndexEntry> indexEntries = listIndexes();
        if (indexEntries != null) {
            for (IndexEntry indexEntry : indexEntries) {
                String field = indexEntry.getField();
                String indexType = indexEntry.getIndexType();
                Indexer indexer = findIndexer(indexType);

                removeIndexEntry(field, document, nitriteId, indexer, indexEntry);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void updateIndex(Document oldDocument, Document newDocument, NitriteId nitriteId) {
        Collection<IndexEntry> indexEntries = listIndexes();
        if (indexEntries != null) {
            for (IndexEntry indexEntry : indexEntries) {
                String field = indexEntry.getField();
                Object newValue = newDocument.get(field);
                Object oldValue = oldDocument.get(field);

                if (newValue == null) continue;
                if (newValue instanceof Comparable && oldValue instanceof Comparable) {
                    if (((Comparable) newValue).compareTo(oldValue) == 0) continue;
                }

                validateDocumentIndexField(newValue, field);

                if (indexCatalog.isDirtyIndex(collectionName, field)
                    && !getBuildFlag(field).get()) {
                    // rebuild will also take care of the current document
                    rebuildIndex(indexEntry, true);
                } else {
                    String indexType = indexEntry.getIndexType();
                    Indexer indexer = findIndexer(indexType);
                    indexer.updateIndex(nitriteMap, nitriteId, field, newValue, oldValue);
                }
            }
        }
    }

    // call to this method is already synchronized, only one thread per field
    // can access it only if rebuild is already not running for that field
    void rebuildIndex(IndexEntry indexEntry, boolean isAsync) {
        final String field = indexEntry.getField();
        if (getBuildFlag(field).compareAndSet(false, true)) {
            if (isAsync) {
                rebuildExecutor.submit(() -> buildIndexInternal(field, indexEntry));
            } else {
                buildIndexInternal(field, indexEntry);
            }
            return;
        }
        throw new IndexingException("indexing is already running on " + indexEntry.getField());
    }

    void dropIndex(String field) {
        if (getBuildFlag(field).get()) {
            throw new IndexingException("cannot drop index as indexing is running on " + field);
        }

        IndexEntry indexEntry = findIndexEntry(field);
        if (indexEntry != null) {
            String indexType = indexEntry.getIndexType();
            Indexer indexer = findIndexer(indexType);
            indexer.dropIndex(nitriteMap, field);
            indexCatalog.dropIndexEntry(collectionName, field);
            indexBuildRegistry.remove(field);
        } else {
            throw new IndexingException(field + " is not indexed");
        }
    }

    void dropAllIndices() {
        for (Map.Entry<String, AtomicBoolean> entry : indexBuildRegistry.entrySet()) {
            if (entry.getValue() != null && entry.getValue().get()) {
                throw new IndexingException("cannot drop index as indexing is running on " + entry.getKey());
            }
        }

        for (IndexEntry index : listIndexes()) {
            dropIndex(index.getField());
        }
        indexBuildRegistry.clear();
    }

    boolean isIndexing(String field) {
        // has an index will only return true, if there is an index on
        // the value and indexing is not running on it
        return indexCatalog.hasIndexEntry(collectionName, field)
            && getBuildFlag(field).get();
    }

    boolean hasIndexEntry(String field) {
        return indexCatalog.hasIndexEntry(collectionName, field);
    }

    Collection<IndexEntry> listIndexes() {
        return indexCatalog.listIndexEntries(collectionName);
    }

    IndexEntry findIndexEntry(String field) {
        return indexCatalog.findIndexEntry(collectionName, field);
    }

    private void init() {
        NitriteStore nitriteStore = nitriteConfig.getNitriteStore();
        this.indexCatalog = nitriteStore.getIndexCatalog();
        this.collectionName = nitriteMap.getName();
        this.indexBuildRegistry = new ConcurrentHashMap<>();
        this.rebuildExecutor = ThreadPoolManager.workerPool();
    }

    private Indexer findIndexer(String indexType) {
        Indexer indexer = nitriteConfig.findIndexer(indexType);
        if (indexer != null) {
            return indexer;
        }
        throw new IndexingException("no indexer found for index type " + indexType);
    }

    private void buildIndexInternal(final String field, final IndexEntry indexEntry) {
        try {
            alert(EventType.IndexStart, field);
            // first put dirty marker
            indexCatalog.beginIndexing(collectionName, field);

            String indexType = indexEntry.getIndexType();
            Indexer indexer = findIndexer(indexType);

            // re-create the index for the values of the field from document
            for (KeyValuePair<NitriteId, Document> entry : nitriteMap.entries()) {
                Document document = entry.getValue();
                if (document.getFields().contains(field)) {
                    // remove old values if exists
                    removeIndexEntry(field, entry.getValue(), entry.getKey(), indexer, indexEntry);

                    // re-create new entry
                    writeIndexEntry(field, entry.getValue(), entry.getKey(), indexer, indexEntry);
                }
            }
        } finally {
            // remove dirty marker to denote indexing completed successfully
            // if dirty marker is found in any index, it needs to be rebuild
            indexCatalog.endIndexing(collectionName, field);
            getBuildFlag(field).set(false);
            alert(EventType.IndexEnd, field);
        }
    }

    private void writeIndexEntry(String field, Document document, NitriteId nitriteId,
                                 Indexer indexer, IndexEntry indexEntry) {
        if (indexEntry != null) {
            Object fieldValue = document.get(field);
            validateDocumentIndexField(fieldValue, field);

            // if dirty index and currently indexing is not running, rebuild
            if (indexCatalog.isDirtyIndex(collectionName, field)
                && !getBuildFlag(field).get()) {
                // rebuild will also take care of the current document
                rebuildIndex(indexEntry, true);
            } else if (indexer != null) {
                indexer.writeIndex(nitriteMap, nitriteId, field, fieldValue);
            }
        }
    }

    private void removeIndexEntry(String field, Document document, NitriteId nitriteId,
                                  Indexer indexer, IndexEntry indexEntry) {
        if (indexEntry != null) {
            Object fieldValue = document.get(field);
            if (fieldValue == null) return;

            validateDocumentIndexField(fieldValue, field);

            // if dirty index and currently indexing is not running, rebuild
            if (indexCatalog.isDirtyIndex(collectionName, field)
                && !getBuildFlag(field).get()) {
                // rebuild will also take care of the current document
                rebuildIndex(indexEntry, true);
            } else if (indexer != null) {
                indexer.removeIndex(nitriteMap, nitriteId, field, fieldValue);
            }
        }
    }

    private AtomicBoolean getBuildFlag(String field) {
        AtomicBoolean flag = indexBuildRegistry.get(field);
        if (flag != null) return flag;

        flag = new AtomicBoolean(false);
        indexBuildRegistry.put(field, flag);
        return flag;
    }

    private void alert(EventType eventType, String field) {
        CollectionEventInfo<String> eventInfo = new CollectionEventInfo<>();
        eventInfo.setItem(field);
        eventInfo.setTimestamp(System.currentTimeMillis());
        eventInfo.setEventType(eventType);
        if (eventBus != null) {
            eventBus.post(eventInfo);
        }
    }

    @Override
    public void close() {
        if (rebuildExecutor != null) {
            this.rebuildExecutor.shutdown();
        }
    }
}
