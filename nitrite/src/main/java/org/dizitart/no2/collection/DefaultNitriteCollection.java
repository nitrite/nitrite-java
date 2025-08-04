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

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.processors.Processor;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
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
 * @since 1.0
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
        notNull(processor, "a null processor cannot be added");

        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.addProcessor(processor);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult insert(Document[] documents) {
        notNull(documents, "a null document cannot be inserted");
        containsNull(documents, "a null document cannot be inserted");

        try {
            writeLock.lock();
            checkOpened();
            return collectionOperations.insert(documents);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult update(Document document, boolean insertIfAbsent) {
        notNull(document, "a null document cannot be used for update");

        if (insertIfAbsent) {
            return update(createUniqueFilter(document), document, updateOptions(true));
        } else {
            if (document.hasId()) {
                return update(createUniqueFilter(document), document, updateOptions(false));
            } else {
                throw new NotIdentifiableException("Update operation failed as the document does not have id");
            }
        }
    }

    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        notNull(update, "a null document cannot be used for update");
        notNull(updateOptions, "updateOptions cannot be null");

        try {
            writeLock.lock();
            checkOpened();
            return collectionOperations.update(filter, update, updateOptions);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult remove(Document document) {
        notNull(document, "a null document cannot be removed");

        if (document.hasId()) {
            try {
                writeLock.lock();
                checkOpened();
                return collectionOperations.remove(document);
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new NotIdentifiableException("Document has no id, cannot remove by document");
        }
    }

    public WriteResult remove(Filter filter, boolean justOne) {
        if ((filter == null || filter == Filter.ALL) && justOne) {
            throw new InvalidOperationException("Cannot remove all documents with justOne set to true");
        }

        try {
            writeLock.lock();
            checkOpened();
            return collectionOperations.remove(filter, justOne);
        } finally {
            writeLock.unlock();
        }
    }

    public void clear() {
        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.find(filter, findOptions);
        } finally {
            readLock.unlock();
        }
    }

    public void createIndex(IndexOptions indexOptions, String... fields) {
        notNull(fields, "fields cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            writeLock.lock();
            checkOpened();

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
        notNull(fields, "fields cannot be null");

        IndexDescriptor indexDescriptor;
        Fields indexFields = Fields.withNames(fields);
        try {
            readLock.lock();
            checkOpened();
            indexDescriptor = collectionOperations.findIndex(indexFields);
        } finally {
            readLock.unlock();
        }

        if (indexDescriptor != null) {
            validateRebuildIndex(indexDescriptor);

            try {
                writeLock.lock();
                checkOpened();
                collectionOperations.rebuildIndex(indexDescriptor);
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new IndexingException(Arrays.toString(fields) + " is not indexed");
        }
    }

    public Collection<IndexDescriptor> listIndices() {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.listIndexes();
        } finally {
            readLock.unlock();
        }
    }

    public boolean hasIndex(String... fields) {
        notNull(fields, "fields cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.hasIndex(indexFields);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isIndexing(String... fields) {
        notNull(fields, "field cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.isIndexing(indexFields);
        } finally {
            readLock.unlock();
        }
    }

    public void dropIndex(String... fields) {
        notNull(fields, "fields cannot be null");

        Fields indexFields = Fields.withNames(fields);
        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.dropIndex(indexFields);
        } finally {
            writeLock.unlock();
        }
    }

    public void dropAllIndices() {
        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.dropAllIndices();
        } finally {
            writeLock.unlock();
        }
    }

    public Document getById(NitriteId nitriteId) {
        notNull(nitriteId, "nitriteId cannot be null");

        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.getById(nitriteId);
        } finally {
            readLock.unlock();
        }
    }

    public void drop() {
        try {
            writeLock.lock();
            checkOpened();

            if (collectionOperations != null) {
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

    public boolean isDropped() {
        try {
            readLock.lock();
            return isDropped || nitriteMap == null || nitriteMap.isDropped();
        } finally {
            readLock.unlock();
        }
    }

    public boolean isOpen() {
        try {
            readLock.lock();
            return nitriteStore != null && !nitriteStore.isClosed()
                && !isDropped && !nitriteMap.isClosed() && !nitriteMap.isDropped();
        } catch (Exception e) {
            throw new NitriteIOException("Failed to check the collection state", e);
        } finally {
            readLock.unlock();
        }
    }

    public void close() {
        try {
            writeLock.lock();
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
        } finally {
            writeLock.unlock();
        }
    }

    public String getName() {
        return collectionName;
    }

    public long size() {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.getSize();
        } finally {
            readLock.unlock();
        }
    }

    public NitriteStore<?> getStore() {
        try {
            writeLock.lock();
            return nitriteStore;
        } finally {
            writeLock.unlock();
        }
    }

    public String subscribe(CollectionEventListener listener) {
        notNull(listener, "listener cannot be null");
        try {
            writeLock.lock();
            checkOpened();
            return eventBus.register(listener);
        } finally {
            writeLock.unlock();
        }
    }

    public void unsubscribe(String subscription) {
        notNull(subscription, "subscription cannot be null");
        try {
            writeLock.lock();
            checkOpened();

            if (eventBus != null) {
                eventBus.deregister(subscription);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public Attributes getAttributes() {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.getAttributes();
        } finally {
            readLock.unlock();
        }
    }

    public void setAttributes(Attributes attributes) {
        notNull(attributes, "attributes cannot be null");

        try {
            writeLock.lock();
            checkOpened();
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
        throw new NitriteIOException("Collection is closed");
    }

    private void validateRebuildIndex(IndexDescriptor indexDescriptor) {
        notNull(indexDescriptor, "index cannot be null");

        String[] indexFields = indexDescriptor.getFields().getFieldNames().toArray(new String[0]);
        if (isIndexing(indexFields)) {
            throw new IndexingException("Cannot rebuild index, index is currently being built");
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
