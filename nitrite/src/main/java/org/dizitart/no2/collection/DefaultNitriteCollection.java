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
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.concurrent.LockService;
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
                throw new NotIdentifiableException("update operation failed as no id value found for the document");
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
            throw new NotIdentifiableException("remove operation failed as no id value found for the document");
        }
    }

    public WriteResult remove(Filter filter, boolean justOne) {
        if ((filter == null || filter == Filter.ALL) && justOne) {
            throw new InvalidOperationException("remove all cannot be combined with just once");
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
            nitriteMap.clear();
        } finally {
            writeLock.unlock();
        }
    }

<<<<<<< Updated upstream
    public DocumentCursor find() {
        checkOpened();
        try {
            readLock.lock();
            return collectionOperations.find();
=======
    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.find(filter, findOptions);
>>>>>>> Stashed changes
        } finally {
            readLock.unlock();
        }
    }

<<<<<<< Updated upstream
    public DocumentCursor find(Filter filter) {
        checkOpened();
=======
    public void createIndex(IndexOptions indexOptions, String... fields) {
        notNull(fields, "fields cannot be null");
>>>>>>> Stashed changes

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
            checkOpened();
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

<<<<<<< Updated upstream
    public void rebuildIndex(String field, boolean isAsync) {
        checkOpened();
        notNull(field, "field cannot be null");
=======
    public void rebuildIndex(String... fields) {
        notNull(fields, "fields cannot be null");
>>>>>>> Stashed changes

        IndexEntry indexEntry;
        try {
            readLock.lock();
<<<<<<< Updated upstream
            indexEntry = collectionOperations.findIndex(field);
=======
            checkOpened();
            indexDescriptor = collectionOperations.findIndex(indexFields);
>>>>>>> Stashed changes
        } finally {
            readLock.unlock();
        }

        if (indexEntry != null) {
            validateRebuildIndex(indexEntry);

            try {
                writeLock.lock();
<<<<<<< Updated upstream
                collectionOperations.rebuildIndex(indexEntry, isAsync);
=======
                checkOpened();
                collectionOperations.rebuildIndex(indexDescriptor);
>>>>>>> Stashed changes
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new IndexingException(field + " is not indexed");
        }
    }

<<<<<<< Updated upstream
    public Collection<IndexEntry> listIndices() {
        checkOpened();

=======
    public Collection<IndexDescriptor> listIndices() {
>>>>>>> Stashed changes
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.listIndexes();
        } finally {
            readLock.unlock();
        }
    }

<<<<<<< Updated upstream
    public boolean hasIndex(String field) {
        checkOpened();
        notNull(field, "field cannot be null");
=======
    public boolean hasIndex(String... fields) {
        notNull(fields, "fields cannot be null");
>>>>>>> Stashed changes

        try {
            readLock.lock();
<<<<<<< Updated upstream
            return collectionOperations.hasIndex(field);
=======
            checkOpened();
            return collectionOperations.hasIndex(indexFields);
>>>>>>> Stashed changes
        } finally {
            readLock.unlock();
        }
    }

<<<<<<< Updated upstream
    public boolean isIndexing(String field) {
        checkOpened();
        notNull(field, "field cannot be null");
=======
    public boolean isIndexing(String... fields) {
        notNull(fields, "field cannot be null");
>>>>>>> Stashed changes

        try {
            readLock.lock();
<<<<<<< Updated upstream
            return collectionOperations.isIndexing(field);
=======
            checkOpened();
            return collectionOperations.isIndexing(indexFields);
>>>>>>> Stashed changes
        } finally {
            readLock.unlock();
        }
    }

<<<<<<< Updated upstream
    public void dropIndex(String field) {
        checkOpened();
        notNull(field, "field cannot be null");
=======
    public void dropIndex(String... fields) {
        notNull(fields, "fields cannot be null");
>>>>>>> Stashed changes

        try {
            writeLock.lock();
<<<<<<< Updated upstream
            collectionOperations.dropIndex(field);
=======
            checkOpened();
            collectionOperations.dropIndex(indexFields);
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
            collectionOperations.dropCollection();
=======
            checkOpened();

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
>>>>>>> Stashed changes
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
            checkOpened();
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
