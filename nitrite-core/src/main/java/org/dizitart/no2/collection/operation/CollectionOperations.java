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
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Anindya Chatterjee
 */
public class CollectionOperations {
    private final String collectionName;
    private final NitriteConfig nitriteConfig;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private IndexOperations indexOperations;
    private WriteOperations writeOperations;
    private ReadOperations readOperations;
    private Lock readLock;
    private Lock writeLock;

    public CollectionOperations(String collectionName,
                                NitriteMap<NitriteId, Document> nitriteMap,
                                NitriteConfig nitriteConfig,
                                EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus) {
        this.collectionName = collectionName;
        this.nitriteMap = nitriteMap;
        this.nitriteConfig = nitriteConfig;
        this.eventBus = eventBus;
        init();
    }

    public void createIndex(String field, String indexType, boolean async) {
        try {
            writeLock.lock();
            indexOperations.ensureIndex(field, indexType, async);
        } finally {
            writeLock.unlock();
        }
    }

    public IndexEntry findIndex(String field) {
        try {
            readLock.lock();
            return indexOperations.findIndexEntry(field);
        } finally {
            readLock.unlock();
        }
    }

    public void rebuildIndex(IndexEntry indexEntry, boolean async) {
        try {
            writeLock.lock();
            indexOperations.rebuildIndex(indexEntry, async);
        } finally {
            writeLock.unlock();
        }
    }

    public Collection<IndexEntry> listIndexes() {
        try {
            readLock.lock();
            return indexOperations.listIndexes();
        } finally {
            readLock.unlock();
        }
    }

    public boolean hasIndex(String field) {
        try {
            readLock.lock();
            return indexOperations.hasIndexEntry(field);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isIndexing(String field) {
        try {
            readLock.lock();
            return indexOperations.isIndexing(field);
        } finally {
            readLock.unlock();
        }
    }

    public void dropIndex(String field) {
        try {
            writeLock.lock();
            indexOperations.dropIndex(field);
        } finally {
            writeLock.unlock();
        }
    }

    public void dropAllIndices() {
        try {
            writeLock.lock();
            indexOperations.dropAllIndices();
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult insert(Document[] documents) {
        try {
            writeLock.lock();
            return writeOperations.insert(documents);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        try {
            writeLock.lock();
            return writeOperations.update(filter, update, updateOptions);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult remove(Document document) {
        try {
            writeLock.lock();
            return writeOperations.remove(document);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult remove(Filter filter, boolean justOnce) {
        try {
            writeLock.lock();
            return writeOperations.remove(filter, justOnce);
        } finally {
            writeLock.unlock();
        }
    }

    public DocumentCursor find() {
        try {
            readLock.lock();
            return readOperations.find();
        } finally {
            readLock.unlock();
        }
    }

    public DocumentCursor find(Filter filter) {
        try {
            readLock.lock();
            return readOperations.find(filter);
        } finally {
            readLock.unlock();
        }
    }

    public Document getById(NitriteId nitriteId) {
        try {
            readLock.lock();
            return readOperations.getById(nitriteId);
        } finally {
            readLock.unlock();
        }
    }

    public void dropCollection() {
        try {
            writeLock.lock();
            indexOperations.dropAllIndices();
            nitriteMap.drop();
        } finally {
            writeLock.unlock();
        }
    }

    public long getSize() {
        try {
            readLock.lock();
            return nitriteMap.size();
        } finally {
            readLock.unlock();
        }
    }

    public Attributes getAttributes() {
        try {
            readLock.lock();
            return nitriteMap != null ? nitriteMap.getAttributes() : null;
        } finally {
            readLock.unlock();
        }
    }

    public void setAttributes(Attributes attributes) {
        try {
            writeLock.lock();
            nitriteMap.setAttributes(attributes);
        } finally {
            writeLock.unlock();
        }
    }

    public void close() {
        if (indexOperations != null) {
            indexOperations.close();
        }
    }

    private void init() {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
        this.indexOperations = new IndexOperations(nitriteConfig, nitriteMap, eventBus);
        this.readOperations = new ReadOperations(collectionName, nitriteConfig, nitriteMap);
        this.writeOperations = new WriteOperations(indexOperations, readOperations,
            nitriteMap, eventBus);
    }
}
