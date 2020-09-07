package org.dizitart.no2.collection;

import lombok.Getter;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.common.util.DocumentUtils.createUniqueFilter;
import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
class BaseNitriteCollection {
    protected final String collectionName;
    protected NitriteMap<NitriteId, Document> nitriteMap;
    protected NitriteStore<?> nitriteStore;
    protected NitriteConfig nitriteConfig;
    protected Lock writeLock;
    private Lock readLock;
    private CollectionOperations collectionOperations;
    private EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;

    @Getter
    private volatile boolean isDropped;

    BaseNitriteCollection(String name, NitriteMap<NitriteId, Document> nitriteMap, NitriteConfig nitriteConfig) {
        this.collectionName = name;
        this.nitriteConfig = nitriteConfig;
        this.nitriteMap = nitriteMap;
        init();
    }

    public WriteResult insert(Document[] documents) {
        checkOpened();
        notNull(documents, "a null document cannot be inserted");
        containsNull(documents, "a null document cannot be inserted");

        try {
            writeLock.lock();
            return collectionOperations.insert(documents);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult update(Document document, boolean insertIfAbsent) {
        checkOpened();
        notNull(document, "a null document cannot be used for update");

        if (insertIfAbsent) {
            return update(createUniqueFilter(document), document, updateOptions(true));
        } else {
            if (document.hasId()) {
                return update(createUniqueFilter(document), document, updateOptions(false));
            } else {
                throw new NotIdentifiableException("update operation failed as no id value found for the document");
            }
        }
    }

    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        checkOpened();
        notNull(update, "a null document cannot be used for update");
        notNull(updateOptions, "updateOptions cannot be null");

        try {
            writeLock.lock();
            return collectionOperations.update(filter, update, updateOptions);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult remove(Document document) {
        checkOpened();
        notNull(document, "a null document cannot be removed");

        if (document.hasId()) {
            try {
                writeLock.lock();
                return collectionOperations.remove(document);
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new NotIdentifiableException("remove operation failed as no id value found for the document");
        }
    }

    public WriteResult remove(Filter filter, boolean justOne) {
        checkOpened();
        if ((filter == null || filter == Filter.ALL) && justOne) {
            throw new InvalidOperationException("remove all cannot be combined with just once");
        }

        try {
            writeLock.lock();
            return collectionOperations.remove(filter, justOne);
        } finally {
            writeLock.unlock();
        }
    }

    public void clear() {
        checkOpened();
        try {
            writeLock.lock();
            nitriteMap.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public DocumentCursor find() {
        checkOpened();
        try {
            readLock.lock();
            return collectionOperations.find();
        } finally {
            readLock.unlock();
        }
    }

    public DocumentCursor find(Filter filter) {
        checkOpened();

        try {
            readLock.lock();
            return collectionOperations.find(filter);
        } finally {
            readLock.unlock();
        }
    }

    public void createIndex(String field, IndexOptions indexOptions) {
        checkOpened();
        notNull(field, "field cannot be null");

        // by default async is false while creating index
        try {
            writeLock.lock();
            if (indexOptions == null) {
                collectionOperations.createIndex(field, IndexType.Unique, false);
            } else {
                collectionOperations.createIndex(field, indexOptions.getIndexType(),
                    indexOptions.isAsync());
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void rebuildIndex(String field, boolean isAsync) {
        checkOpened();
        notNull(field, "field cannot be null");

        IndexEntry indexEntry;
        try {
            readLock.lock();
            indexEntry = collectionOperations.findIndex(field);
        } finally {
            readLock.unlock();
        }

        if (indexEntry != null) {
            validateRebuildIndex(indexEntry);

            try {
                writeLock.lock();
                collectionOperations.rebuildIndex(indexEntry, isAsync);
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new IndexingException(field + " is not indexed");
        }
    }

    public Collection<IndexEntry> listIndices() {
        checkOpened();

        try {
            readLock.lock();
            return collectionOperations.listIndexes();
        } finally {
            readLock.unlock();
        }
    }

    public boolean hasIndex(String field) {
        checkOpened();
        notNull(field, "field cannot be null");

        try {
            readLock.lock();
            return collectionOperations.hasIndex(field);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isIndexing(String field) {
        checkOpened();
        notNull(field, "field cannot be null");

        try {
            readLock.lock();
            return collectionOperations.isIndexing(field);
        } finally {
            readLock.unlock();
        }
    }

    public void dropIndex(String field) {
        checkOpened();
        notNull(field, "field cannot be null");

        try {
            writeLock.lock();
            collectionOperations.dropIndex(field);
        } finally {
            writeLock.unlock();
        }
    }

    public void dropAllIndices() {
        checkOpened();

        try {
            writeLock.lock();
            collectionOperations.dropAllIndices();
        } finally {
            writeLock.unlock();
        }
    }

    public Document getById(NitriteId nitriteId) {
        checkOpened();
        notNull(nitriteId, "nitriteId cannot be null");

        try {
            readLock.lock();
            return collectionOperations.getById(nitriteId);
        } finally {
            readLock.unlock();
        }
    }

    public void drop() {
        checkOpened();

        try {
            writeLock.lock();
            collectionOperations.dropCollection();
        } finally {
            writeLock.unlock();
        }
        isDropped = true;
        close();
    }

    public boolean isOpen() {
        if (nitriteStore == null || nitriteStore.isClosed() || isDropped) {
            close();
            return false;
        } else return true;
    }

    public void close() {
        if (collectionOperations != null) {
            collectionOperations.close();
        }
        this.nitriteMap = null;
        this.nitriteConfig = null;
        this.collectionOperations = null;
        this.nitriteStore = null;
        closeEventBus();
    }

    public String getName() {
        return collectionName;
    }

    public long size() {
        try {
            readLock.lock();
            return collectionOperations.getSize();
        } finally {
            readLock.unlock();
        }
    }

    public NitriteStore<?> getStore() {
        return nitriteStore;
    }

    public void subscribe(CollectionEventListener listener) {
        checkOpened();
        notNull(listener, "listener cannot be null");

        eventBus.register(listener);
    }

    public void unsubscribe(CollectionEventListener listener) {
        checkOpened();
        notNull(listener, "listener cannot be null");

        if (eventBus != null) {
            eventBus.deregister(listener);
        }
    }

    public Attributes getAttributes() {
        checkOpened();

        try {
            readLock.lock();
            return collectionOperations.getAttributes();
        } finally {
            readLock.unlock();
        }
    }

    public void setAttributes(Attributes attributes) {
        checkOpened();
        notNull(attributes, "attributes cannot be null");

        try {
            writeLock.lock();
            collectionOperations.setAttributes(attributes);
        } finally {
            writeLock.unlock();
        }
    }

    private void closeEventBus() {
        if (eventBus != null) {
            eventBus.close();
        }
        eventBus = null;
    }

    private void init() {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
        this.nitriteStore = nitriteConfig.getNitriteStore();
        this.eventBus = new CollectionEventBus();
        this.collectionOperations = new CollectionOperations(collectionName, nitriteMap, nitriteConfig, eventBus);
    }

    protected void checkOpened() {
        if (isOpen()) return;

        if (isDropped) {
            throw new NitriteIOException("collection has been dropped");
        }

        if (nitriteStore == null || nitriteStore.isClosed()) {
            throw new NitriteIOException("store is closed");
        }
    }

    private void validateRebuildIndex(IndexEntry indexEntry) {
        notNull(indexEntry, "index cannot be null");

        if (isIndexing(indexEntry.getField())) {
            throw new IndexingException("indexing on value " + indexEntry.getField() + " is currently running");
        }
    }

    private static class CollectionEventBus extends NitriteEventBus<CollectionEventInfo<?>, CollectionEventListener> {

        public void post(CollectionEventInfo<?> collectionEventInfo) {
            for (final CollectionEventListener listener : getListeners()) {
                getEventExecutor().submit(() -> listener.onEvent(collectionEventInfo));
            }
        }
    }
}
