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
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static org.dizitart.no2.common.Constants.COLLECTION_CATALOG;
import static org.dizitart.no2.common.Constants.TAG_COLLECTIONS;
import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
public class CollectionFactory {
    private final Map<String, NitriteCollection> collectionMap;
    private final ReentrantLock lock;

    public CollectionFactory() {
        collectionMap = new HashMap<>();
        lock = new ReentrantLock();
    }

    public NitriteCollection getCollection(String name, NitriteConfig nitriteConfig, boolean writeCatalogue) {
        notNull(nitriteConfig, "configuration is null while creating collection");
        notEmpty(name, "collection name is null or empty");

        try {
            lock.lock();
            if (collectionMap.containsKey(name)) {
                NitriteCollection collection = collectionMap.get(name);
                if (collection.isDropped()) {
                    collectionMap.remove(name);
                    return createCollection(name, nitriteConfig, writeCatalogue);
                }
                return collectionMap.get(name);
            } else {
                return createCollection(name, nitriteConfig, writeCatalogue);
            }
        } finally {
            lock.unlock();
        }
    }

    private NitriteCollection createCollection(String name, NitriteConfig nitriteConfig, boolean writeCatalog) {
        NitriteStore store = nitriteConfig.getNitriteStore();
        NitriteMap<NitriteId, Document> nitriteMap = store.openMap(name);
        NitriteCollection collection = new NitriteCollectionImpl(name, nitriteMap, nitriteConfig);

        if (writeCatalog) {
            // ignore repository request
            if (store.getRepositoryRegistry().contains(name)) {
                throw new ValidationException("a repository with same name already exists");
            }

            for (Set<String> set : store.getKeyedRepositoryRegistry().values()) {
                if (set.contains(name)) {
                    throw new ValidationException("a keyed repository with same name already exists");
                }
            }

            collectionMap.put(name, collection);
            NitriteMap<String, Document> catalogMap = store.openMap(COLLECTION_CATALOG);
            Document document = catalogMap.get(TAG_COLLECTIONS);
            if (document == null) document = Document.createDocument();
            document.put(name, true);
            catalogMap.put(TAG_COLLECTIONS, document);
        }

        return collection;
    }

    public void clear() {
        try {
            lock.lock();
            for (NitriteCollection collection : collectionMap.values()) {
                collection.close();
            }
            collectionMap.clear();
        } finally {
            lock.unlock();
        }
    }
}
