package org.dizitart.no2.collection.operation;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.DocumentUtils;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.concurrent.ThreadPoolManager.runAsync;

/**
 * @author Anindya Chatterjee
 * @since 1.0
 */
class IndexOperations implements AutoCloseable {
    private final String collectionName;
    private final NitriteConfig nitriteConfig;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private final Map<Fields, AtomicBoolean> indexBuildTracker;
    private IndexManager indexManager;

    IndexOperations(String collectionName, NitriteConfig nitriteConfig,
                    NitriteMap<NitriteId, Document> nitriteMap,
                    EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus) {
        this.collectionName = collectionName;
        this.nitriteConfig = nitriteConfig;
        this.nitriteMap = nitriteMap;
        this.eventBus = eventBus;
        this.indexBuildTracker = new ConcurrentHashMap<>();
        this.indexManager = new IndexManager(collectionName, nitriteConfig);
    }

    @Override
    public void close() {
        indexManager.close();
    }

    void createIndex(Fields fields, String indexType) {
        IndexDescriptor indexDescriptor = indexManager.findExactIndexDescriptor(fields);
        if (indexDescriptor == null) {
            // if no index create index
            indexDescriptor = indexManager.createIndexDescriptor(fields, indexType);
        } else {
            // if index already there throw
            throw new IndexingException("Index already exists on fields: " + fields);
        }

        buildIndex(indexDescriptor, false);
    }

    // call to this method is already synchronized, only one thread per field
    // can access it only if rebuild is already not running for that field
    void buildIndex(IndexDescriptor indexDescriptor, boolean rebuild) {
        final Fields fields = indexDescriptor.getFields();
        if (getBuildFlag(fields).compareAndSet(false, true)) {
            buildIndexInternal(indexDescriptor, rebuild);
            return;
        }
        throw new IndexingException("Index build already in progress on fields: " + indexDescriptor.getFields());
    }

    void dropIndex(Fields fields) {
        if (getBuildFlag(fields).get()) {
            throw new IndexingException("Index build already in progress on fields: " + fields);
        }

        IndexDescriptor indexDescriptor = findIndexDescriptor(fields);
        if (indexDescriptor != null) {
            String indexType = indexDescriptor.getIndexType();
            NitriteIndexer nitriteIndexer = nitriteConfig.findIndexer(indexType);
            nitriteIndexer.dropIndex(indexDescriptor, nitriteConfig);

            indexManager.dropIndexDescriptor(fields);
            indexBuildTracker.remove(fields);
        } else {
            throw new IndexingException("Index does not exist on fields: " + fields);
        }
    }

    void dropAllIndices() {
        for (Map.Entry<Fields, AtomicBoolean> entry : indexBuildTracker.entrySet()) {
            if (entry.getValue() != null && entry.getValue().get()) {
                throw new IndexingException("Index build already in progress on fields: " + entry.getKey());
            }
        }

        // we can drop all indices in parallel
        List<Future<?>> futures = new ArrayList<>();
        for (IndexDescriptor index : listIndexes()) {
            futures.add(runAsync(() -> dropIndex(index.getFields())));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IndexingException("Failed to drop all indices", e);
            }
        }

        indexManager.dropIndexMeta();
        indexBuildTracker.clear();
        indexManager.close();

        // recreate index manager to discard old native resources
        // special measure for RocksDB adapter
        indexManager = new IndexManager(collectionName, nitriteConfig);
    }

    void clear() {
        for (Map.Entry<Fields, AtomicBoolean> entry : indexBuildTracker.entrySet()) {
            if (entry.getValue() != null && entry.getValue().get()) {
                throw new IndexingException("Index build already in progress on fields: " + entry.getKey());
            }
        }

        indexManager.clearAll();
        indexBuildTracker.clear();
    }

    boolean isIndexing(Fields field) {
        // has an index will only return true, if there is an index on
        // the value and indexing is not running on it
        return indexManager.hasIndexDescriptor(field)
            && getBuildFlag(field).get();
    }

    boolean hasIndexEntry(Fields field) {
        return indexManager.hasIndexDescriptor(field);
    }

    Collection<IndexDescriptor> listIndexes() {
        return indexManager.getIndexDescriptors();
    }

    IndexDescriptor findIndexDescriptor(Fields field) {
        return indexManager.findExactIndexDescriptor(field);
    }

    boolean shouldRebuildIndex(Fields fields) {
        return indexManager.isDirtyIndex(fields) && !getBuildFlag(fields).get();
    }

    private AtomicBoolean getBuildFlag(Fields field) {
        AtomicBoolean flag = indexBuildTracker.get(field);
        if (flag != null) return flag;

        flag = new AtomicBoolean(false);
        indexBuildTracker.put(field, flag);
        return flag;
    }

    private void buildIndexInternal(IndexDescriptor indexDescriptor, boolean rebuild) {
        Fields fields = indexDescriptor.getFields();
        try {
            alert(EventType.IndexStart, fields);
            // first put dirty marker
            indexManager.beginIndexing(fields);

            String indexType = indexDescriptor.getIndexType();
            NitriteIndexer nitriteIndexer = nitriteConfig.findIndexer(indexType);

            // if rebuild drop existing index
            if (rebuild) {
                nitriteIndexer.dropIndex(indexDescriptor, nitriteConfig);
            }

            for (Pair<NitriteId, Document> entry : nitriteMap.entries()) {
                Document document = entry.getSecond();
                FieldValues fieldValues = DocumentUtils.getValues(document, indexDescriptor.getFields());
                nitriteIndexer.writeIndexEntry(fieldValues, indexDescriptor, nitriteConfig);
            }
        } finally {
            // remove dirty marker to denote indexing completed successfully
            // if dirty marker is found in any index, it needs to be rebuilt
            indexManager.endIndexing(fields);
            getBuildFlag(fields).set(false);
            alert(EventType.IndexEnd, fields);
        }
    }

    private void alert(EventType eventType, Fields field) {
        CollectionEventInfo<Fields> eventInfo = new CollectionEventInfo<>();
        eventInfo.setItem(field);
        eventInfo.setTimestamp(System.currentTimeMillis());
        eventInfo.setEventType(eventType);
        if (eventBus != null) {
            eventBus.post(eventInfo);
        }
    }
}
