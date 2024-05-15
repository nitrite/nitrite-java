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

package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.CollectionFactory;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreCatalog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.dizitart.no2.common.util.ObjectUtils.findRepositoryName;
import static org.dizitart.no2.common.util.ObjectUtils.findRepositoryNameByDecorator;
import static org.dizitart.no2.common.util.ValidationUtils.validateRepositoryType;

/**
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class RepositoryFactory {
    private final Map<String, ObjectRepository<?>> repositoryMap;
    private final CollectionFactory collectionFactory;
    private final ReentrantLock lock;

    public RepositoryFactory(CollectionFactory collectionFactory) {
        this.collectionFactory = collectionFactory;
        this.repositoryMap = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public <T> ObjectRepository<T> getRepository(NitriteConfig nitriteConfig, Class<T> type) {
        return getRepository(nitriteConfig, type, null);
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectRepository<T> getRepository(NitriteConfig nitriteConfig, Class<T> type, String key) {
        if (type == null) {
            throw new ValidationException("type cannot be null");
        }

        if (nitriteConfig == null) {
            throw new ValidationException("nitriteConfig cannot be null");
        }

        String collectionName = findRepositoryName(type, key);

        try {
            lock.lock();
            if (repositoryMap.containsKey(collectionName)) {
                ObjectRepository<T> repository = (ObjectRepository<T>) repositoryMap.get(collectionName);
                if (repository.isDropped() || !repository.isOpen()) {
                    repositoryMap.remove(collectionName);
                    return createRepository(nitriteConfig, type, collectionName, key);
                } else {
                    return repository;
                }
            } else {
                return createRepository(nitriteConfig, type, collectionName, key);
            }
        } finally {
            lock.unlock();
        }
    }

    public <T> ObjectRepository<T> getRepository(NitriteConfig nitriteConfig, EntityDecorator<T> entityDecorator) {
        return getRepository(nitriteConfig, entityDecorator, null);
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectRepository<T> getRepository(NitriteConfig nitriteConfig, EntityDecorator<T> entityDecorator, String key) {
        if (entityDecorator == null) {
            throw new ValidationException("entityDecorator cannot be null");
        }

        if (nitriteConfig == null) {
            throw new ValidationException("nitriteConfig cannot be null");
        }

        String collectionName = findRepositoryNameByDecorator(entityDecorator, key);

        try {
            lock.lock();
            if (repositoryMap.containsKey(collectionName)) {
                ObjectRepository<T> repository = (ObjectRepository<T>) repositoryMap.get(collectionName);
                if (repository.isDropped() || !repository.isOpen()) {
                    repositoryMap.remove(collectionName);
                    return createRepositoryByDecorator(nitriteConfig, entityDecorator, collectionName, key);
                } else {
                    return repository;
                }
            } else {
                return createRepositoryByDecorator(nitriteConfig, entityDecorator, collectionName, key);
            }
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        try {
            lock.lock();
            for (ObjectRepository<?> repository : repositoryMap.values()) {
                repository.close();
            }
            repositoryMap.clear();
        } catch (Exception e) {
            throw new NitriteIOException("Failed to clear an object repository", e);
        } finally {
            lock.unlock();
        }
    }

    private <T> ObjectRepository<T> createRepository(NitriteConfig nitriteConfig, Class<T> type,
                                                     String collectionName, String key) {
        NitriteStore<?> store = nitriteConfig.getNitriteStore();

        validateRepositoryType(type, nitriteConfig);

        if (store.getCollectionNames().contains(collectionName)) {
            throw new ValidationException("A collection with same entity name already exists");
        }

        NitriteCollection nitriteCollection = collectionFactory.getCollection(collectionName,
            nitriteConfig, false);
        ObjectRepository<T> repository = new DefaultObjectRepository<>(type, nitriteCollection, nitriteConfig);
        repositoryMap.put(collectionName, repository);

        writeCatalog(store, collectionName, key);
        return repository;
    }

    private <T> ObjectRepository<T> createRepositoryByDecorator(NitriteConfig nitriteConfig,
                                                                EntityDecorator<T> entityDecorator,
                                                                String collectionName, String key) {
        NitriteStore<?> store = nitriteConfig.getNitriteStore();

        if (store.getCollectionNames().contains(collectionName)) {
            throw new ValidationException("A collection with same entity name already exists");
        }

        validateRepositoryType(entityDecorator.getEntityType(), nitriteConfig);

        NitriteCollection nitriteCollection = collectionFactory.getCollection(collectionName,
            nitriteConfig, false);

        ObjectRepository<T> repository = new DefaultObjectRepository<>(entityDecorator, nitriteCollection, nitriteConfig);
        repositoryMap.put(collectionName, repository);

        writeCatalog(store, collectionName, key);
        return repository;
    }

    private void writeCatalog(NitriteStore<?> store, String name, String key) {
        StoreCatalog storeCatalog = store.getCatalog();
        if (!storeCatalog.hasEntry(name)) {
            if (StringUtils.isNullOrEmpty(key)) {
                storeCatalog.writeRepositoryEntry(name);
            } else {
                storeCatalog.writeKeyedRepositoryEntry(name);
            }
        }
    }
}
