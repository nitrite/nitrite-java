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

package org.dizitart.no2.collection;

import lombok.Getter;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.common.processors.Processor;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.common.util.DocumentUtils.createUniqueFilter;
import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee.
 */
class DefaultNitriteCollection implements NitriteCollection {
    private final String collectionName;
    private final LockService lockService;

    protected NitriteMap<NitriteId, Document> nitriteMap;
    protected NitriteConfig nitriteConfig;
    protected NitriteStore<?> nitriteStore;

    private Lock writeLock;
    private Lock readLock;
    private CollectionOperations collectionOperations;
    private EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;

    @Getter
    private volatile boolean isDropped;

    DefaultNitriteCollection(String name, NitriteMap<NitriteId, Document> nitriteMap,
                             NitriteConfig nitriteConfig, LockService lockService) {
        this.collectionName = name;
        this.nitriteConfig = nitriteConfig;
        this.nitriteMap = nitriteMap;
        this.lockService = lockService;

        initialize();
    }

    @Override
    public void addProcessor(Processor processor) {
        checkOpened();
        notNull(processor, "a null processor cannot be added");
        collectionOperations.addProcessor(processor);
    }

    @Override
    public void removeProcessor(Processor processor) {
        checkOpened();
        notNull(processor, "a null processor cannot be removed");
        collectionOperations.removeProcessor(processor);
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

    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        checkOpened();
        try {
            readLock.lock();
            return collectionOperations.find(filter, findOptions);
        } finally {
            readLock.unlock();
        }
    }

    public void createIndex(IndexOptions indexOptions, String... fields) {
        checkOpened();
        notNull(fields, "fields cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            writeLock.lock();
            if (indexOptions == null) {
                collectionOperations.createIndex(indexFields, IndexType.UNIQUE);
            } else {
                collectionOperations.createIndex(indexFields, indexOptions.getIndexType());
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void rebuildIndex(String... fields) {
        checkOpened();
        notNull(fields, "fields cannot be null");

        IndexDescriptor indexDescriptor;
        Fields indexFields = Fields.withNames(fields);
        try {
            readLock.lock();
            indexDescriptor = collectionOperations.findIndex(indexFields);
        } finally {
            readLock.unlock();
        }

        if (indexDescriptor != null) {
            validateRebuildIndex(indexDescriptor);

            try {
                writeLock.lock();
                collectionOperations.rebuildIndex(indexDescriptor);
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new IndexingException(Arrays.toString(fields) + " is not indexed");
        }
    }

    public Collection<IndexDescriptor> listIndices() {
        checkOpened();

        try {
            readLock.lock();
            return collectionOperations.listIndexes();
        } finally {
            readLock.unlock();
        }
    }

    public boolean hasIndex(String... fields) {
        checkOpened();
        notNull(fields, "fields cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            readLock.lock();
            return collectionOperations.hasIndex(indexFields);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isIndexing(String... fields) {
        checkOpened();
        notNull(fields, "field cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            readLock.lock();
            return collectionOperations.isIndexing(indexFields);
        } finally {
            readLock.unlock();
        }
    }

    public void dropIndex(String... fields) {
        checkOpened();
        notNull(fields, "fields cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            writeLock.lock();
            collectionOperations.dropIndex(indexFields);
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

            if (collectionOperations != null) {
                // close collection and indexes
                collectionOperations.close();

                // drop collection and indexes
                collectionOperations.dropCollection();
            }

            // set all reference to null
            this.nitriteMap = null;
            this.nitriteConfig = null;
            this.collectionOperations = null;
            this.nitriteStore = null;

            // close event bus
            closeEventBus();
        } finally {
            writeLock.unlock();
        }
        isDropped = true;
    }

    public boolean isOpen() {
        if (nitriteStore == null || nitriteStore.isClosed() || isDropped) {
            try {
                close();
            } catch (Exception e) {
                throw new NitriteIOException("failed to close the database", e);
            }
            return false;
        } else return true;
    }

    public void close() {
        if (collectionOperations != null) {
            // close collection and indexes
            collectionOperations.close();
        }

        // set all reference to null
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
        checkOpened();

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

    private void initialize() {
        this.isDropped = false;
        this.readLock = lockService.getReadLock(collectionName);
        this.writeLock = lockService.getWriteLock(collectionName);
        this.nitriteStore = nitriteConfig.getNitriteStore();
        this.eventBus = new CollectionEventBus();
        this.collectionOperations = new CollectionOperations(collectionName, nitriteMap, nitriteConfig, eventBus);
    }

    private void checkOpened() {
        if (isOpen()) return;

        if (isDropped) {
            throw new NitriteIOException("collection has been dropped");
        }

        if (nitriteStore == null || nitriteStore.isClosed()) {
            throw new NitriteIOException("store is closed");
        }
    }

    private void validateRebuildIndex(IndexDescriptor indexDescriptor) {
        notNull(indexDescriptor, "index cannot be null");

        String[] indexFields = indexDescriptor.getIndexFields().getFieldNames().toArray(new String[0]);
        if (isIndexing(indexFields)) {
            throw new IndexingException("indexing on value " + indexDescriptor.getIndexFields() + " is currently running");
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
