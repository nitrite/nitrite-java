package org.dizitart.no2.collection.operation;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.DocumentUtils;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.concurrent.ThreadPoolManager.runAsync;

/**
 * @author Anindya Chatterjee
 */
class IndexOperations implements AutoCloseable {
    private final NitriteConfig nitriteConfig;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;

    private String collectionName;
    private IndexCatalog indexCatalog;
    private Map<Fields, AtomicBoolean> indexBuildRegistry;
    private ExecutorService rebuildExecutor;
    private Collection<IndexDescriptor> indexDescriptorCache;

    IndexOperations(NitriteConfig nitriteConfig, NitriteMap<NitriteId, Document> nitriteMap,
                    EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus) {
        this.nitriteConfig = nitriteConfig;
        this.nitriteMap = nitriteMap;
        this.eventBus = eventBus;
        initialize();
    }

    @Override
    public void close() {
        if (rebuildExecutor != null) {
            this.rebuildExecutor.shutdown();
        }
    }

    void createIndex(Fields fields, String indexType, boolean isAsync) {
        IndexDescriptor indexDescriptor;
        if (!hasIndexEntry(fields)) {
            // if no index create index
            indexDescriptor = indexCatalog.createIndexDescriptor(collectionName, fields, indexType);
        } else {
            // if index already there throw
            throw new IndexingException("index already exists on " + fields);
        }

        buildIndex(indexDescriptor, isAsync, false);

        // update descriptor cache
        updateIndexDescriptorCache();
    }

    // call to this method is already synchronized, only one thread per field
    // can access it only if rebuild is already not running for that field
    void buildIndex(IndexDescriptor indexDescriptor, boolean isAsync, boolean rebuild) {
        final Fields fields = indexDescriptor.getFields();
        if (getBuildFlag(fields).compareAndSet(false, true)) {
            if (isAsync) {
                rebuildExecutor.submit(() -> buildIndexInternal(indexDescriptor, rebuild));
            } else {
                buildIndexInternal(indexDescriptor, rebuild);
            }
            return;
        }
        throw new IndexingException("indexing is already running on " + indexDescriptor.getFields());
    }

    void dropIndex(Fields fields) {
        if (getBuildFlag(fields).get()) {
            throw new IndexingException("cannot drop index as indexing is running on " + fields);
        }

        IndexDescriptor indexDescriptor = findIndexDescriptor(fields);
        if (indexDescriptor != null) {
            String indexType = indexDescriptor.getIndexType();
            NitriteIndexer nitriteIndexer = nitriteConfig.findIndexer(indexType);
            nitriteIndexer.dropIndex(indexDescriptor, nitriteConfig);

            indexCatalog.dropIndexDescriptor(collectionName, fields);
            indexBuildRegistry.remove(fields);

            // update descriptor cache
            updateIndexDescriptorCache();
        } else {
            throw new IndexingException(fields + " is not indexed");
        }
    }

    void dropAllIndices() {
        for (Map.Entry<Fields, AtomicBoolean> entry : indexBuildRegistry.entrySet()) {
            if (entry.getValue() != null && entry.getValue().get()) {
                throw new IndexingException("cannot drop index as indexing is running on " + entry.getKey());
            }
        }

        List<Future<?>> futures = new ArrayList<>();
        for (IndexDescriptor index : listIndexes()) {
            futures.add(runAsync(() -> dropIndex(index.getFields())));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IndexingException("failed to drop all indices", e);
            }
        }

        indexBuildRegistry.clear();

        // update descriptor cache
        updateIndexDescriptorCache();
    }

    boolean isIndexing(Fields field) {
        // has an index will only return true, if there is an index on
        // the value and indexing is not running on it
        return indexCatalog.hasIndexDescriptor(collectionName, field)
            && getBuildFlag(field).get();
    }

    boolean hasIndexEntry(Fields field) {
        return indexCatalog.hasIndexDescriptor(collectionName, field);
    }

    Collection<IndexDescriptor> listIndexes() {
        return indexDescriptorCache;
    }

    IndexDescriptor findIndexDescriptor(Fields field) {
        return indexCatalog.findIndexDescriptor(collectionName, field);
    }

    AtomicBoolean getBuildFlag(Fields field) {
        AtomicBoolean flag = indexBuildRegistry.get(field);
        if (flag != null) return flag;

        flag = new AtomicBoolean(false);
        indexBuildRegistry.put(field, flag);
        return flag;
    }

    private void initialize() {
        NitriteStore<?> nitriteStore = nitriteConfig.getNitriteStore();
        this.indexCatalog = nitriteStore.getIndexCatalog();
        this.collectionName = nitriteMap.getName();
        this.indexBuildRegistry = new ConcurrentHashMap<>();
        this.rebuildExecutor = ThreadPoolManager.workerPool();
        updateIndexDescriptorCache();
    }

    private void updateIndexDescriptorCache() {
        indexDescriptorCache = indexCatalog.listIndexDescriptors(collectionName);
    }

    private void buildIndexInternal(IndexDescriptor indexDescriptor, boolean rebuild) {
        Fields fields = indexDescriptor.getFields();
        try {
            alert(EventType.IndexStart, fields);
            // first put dirty marker
            indexCatalog.beginIndexing(collectionName, fields);

            String indexType = indexDescriptor.getIndexType();
            NitriteIndexer nitriteIndexer = nitriteConfig.findIndexer(indexType);

            // if rebuild drop existing index
            if (rebuild) {
                nitriteIndexer.dropIndex(indexDescriptor, nitriteConfig);
            }

            for (Pair<NitriteId, Document> entry : nitriteMap.entries()) {
                Document document = entry.getSecond();
                FieldValues fieldValues = DocumentUtils.getValues(document, indexDescriptor.getFields());
                nitriteIndexer.writeIndexEntry(indexDescriptor, fieldValues, nitriteConfig);
            }
        } finally {
            // remove dirty marker to denote indexing completed successfully
            // if dirty marker is found in any index, it needs to be rebuild
            indexCatalog.endIndexing(collectionName, fields);
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
