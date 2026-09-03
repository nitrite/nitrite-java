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
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreCatalog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class CollectionFactory {
    private final Map<String, NitriteCollection> collectionMap;
    private final LockService lockService;

    /**
     * Instantiates a new {@link CollectionFactory}.
     *
     * @param lockService the lock service
     */
    public CollectionFactory(LockService lockService) {
        this.collectionMap = new HashMap<>();
        this.lockService = lockService;
    }

    /**
     * Gets or creates a collection.
     * <p>
     * The factory-wide lock is held only while the registry is read or changed. Whether an
     * already registered collection is still usable is decided outside it, because
     * {@link NitriteCollection#isDropped()} and {@link NitriteCollection#isOpen()} take that
     * collection's own read lock: while a long write (an index rebuild, a large
     * {@code remove(filter)}) holds the collection's write lock, a caller asking for that
     * collection has to wait for it, but with the factory lock held across the wait every
     * caller asking for <em>any</em> collection was queued behind it as well.
     *
     * @param name           the name
     * @param nitriteConfig  the nitrite config
     * @param writeCatalogue to write catalogue
     * @return the collection
     */
    public NitriteCollection getCollection(String name, NitriteConfig nitriteConfig, boolean writeCatalogue) {
        notNull(nitriteConfig, "Configuration is null while creating collection");
        notEmpty(name, "Collection name is null or empty");

        NitriteCollection registered = getRegistered(name);
        if (registered != null && isUsable(registered)) {
            return registered;
        }

        Lock lock = lockService.getWriteLock(this.getClass().getName());
        try {
            lock.lock();
            NitriteCollection current = collectionMap.get(name);
            if (current != null && current != registered && isUsable(current)) {
                // another caller replaced it while this one was checking the old instance
                return current;
            }

            if (current != null) {
                collectionMap.remove(name);
            }
            return createCollection(name, nitriteConfig, writeCatalogue);
        } finally {
            lock.unlock();
        }
    }

    private NitriteCollection getRegistered(String name) {
        Lock lock = lockService.getReadLock(this.getClass().getName());
        try {
            lock.lock();
            return collectionMap.get(name);
        } finally {
            lock.unlock();
        }
    }

    private static boolean isUsable(NitriteCollection collection) {
        return !collection.isDropped() && collection.isOpen();
    }

    private NitriteCollection createCollection(String name, NitriteConfig nitriteConfig, boolean writeCatalog) {
        NitriteStore<?> store = nitriteConfig.getNitriteStore();

        if (writeCatalog) {
            // ignore repository request
            if (store.getRepositoryRegistry().contains(name)) {
                throw new ValidationException("A repository with same name already exists");
            }

            for (Set<String> set : store.getKeyedRepositoryRegistry().values()) {
                if (set.contains(name)) {
                    throw new ValidationException("A keyed repository with same name already exists");
                }
            }
        }

        NitriteMap<NitriteId, Document> nitriteMap = store.openMap(name, NitriteId.class, Document.class);
        NitriteCollection collection = new DefaultNitriteCollection(name, nitriteMap, nitriteConfig, lockService);

        if (writeCatalog) {
            collectionMap.put(name, collection);
            StoreCatalog storeCatalog = store.getCatalog();
            if (!storeCatalog.hasEntry(name)) {
                storeCatalog.writeCollectionEntry(name);
            }
        }

        return collection;
    }

    /**
     * Clears the internal registry holding collection information.
     */
    public void clear() {
        Lock lock = lockService.getWriteLock(this.getClass().getName());
        try {
            lock.lock();
            for (NitriteCollection collection : collectionMap.values()) {
                if (collection.isOpen()) {
                    collection.close();
                }
            }
            collectionMap.clear();
        } catch (Exception e) {
            throw new NitriteIOException("Failed to close a collection", e);
        } finally {
            lock.unlock();
        }
    }
}
