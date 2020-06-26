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

package org.dizitart.no2;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.CollectionFactory;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.RepositoryFactory;
import org.dizitart.no2.store.NitriteStore;

import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class NitriteDatabase implements Nitrite {
    private final CollectionFactory collectionFactory;
    private final RepositoryFactory repositoryFactory;
    private final NitriteConfig nitriteConfig;
    private NitriteStore store;

    NitriteDatabase(NitriteConfig config) {
        this.nitriteConfig = config;
        this.collectionFactory = new CollectionFactory();
        this.repositoryFactory = new RepositoryFactory(collectionFactory);
        this.initialize(null, null);
    }

    NitriteDatabase(String username, String password, NitriteConfig config) {
        validateUserCredentials(username, password);
        this.nitriteConfig = config;
        this.collectionFactory = new CollectionFactory();
        this.repositoryFactory = new RepositoryFactory(collectionFactory);
        this.initialize(username, password);
    }

    @Override
    public NitriteCollection getCollection(String name) {
        validateCollectionName(name);
        checkOpened();
        return collectionFactory.getCollection(name, nitriteConfig, true);
    }

    @Override
    public <T> ObjectRepository<T> getRepository(Class<T> type) {
        checkOpened();
        return repositoryFactory.getRepository(nitriteConfig, type);
    }

    @Override
    public <T> ObjectRepository<T> getRepository(Class<T> type, String key) {
        checkOpened();
        return repositoryFactory.getRepository(nitriteConfig, type, key);
    }

    @Override
    public Set<String> listCollectionNames() {
        checkOpened();
        return store.getCollectionNames();
    }

    @Override
    public Set<String> listRepositories() {
        checkOpened();
        return store.getRepositoryRegistry();
    }

    @Override
    public Map<String, Set<String>> listKeyedRepository() {
        checkOpened();
        return store.getKeyedRepositoryRegistry();
    }

    @Override
    public boolean hasUnsavedChanges() {
        checkOpened();
        return store != null && store.hasUnsavedChanges();
    }

    @Override
    public boolean isClosed() {
        return store == null || store.isClosed();
    }

    @Override
    public NitriteStore getStore() {
        return store;
    }

    @Override
    public NitriteConfig getConfig() {
        return nitriteConfig;
    }

    @Override
    public synchronized void close() {
        checkOpened();
        try {
            store.beforeClose();
            if (hasUnsavedChanges()) {
                log.debug("Unsaved changes detected, committing the changes.");
                commit();
            }

            repositoryFactory.clear();
            collectionFactory.clear();
            store.close();
            log.info("Nitrite database has been closed successfully.");
        } catch (Throwable error) {
            if (!nitriteConfig.getStoreConfig().isReadOnly()) {
                throw new NitriteIOException("error while shutting down nitrite", error);
            }
        }
    }

    @Override
    public void commit() {
        checkOpened();
        if (store != null && !nitriteConfig.getStoreConfig().isReadOnly()) {
            store.commit();
            log.debug("Unsaved changes committed successfully.");
        }
    }

    private void validateUserCredentials(String username, String password) {
        if (StringUtils.isNullOrEmpty(username)) {
            throw new SecurityException("username cannot be empty");
        }
        if (StringUtils.isNullOrEmpty(password)) {
            throw new SecurityException("password cannot be empty");
        }
    }

    private void initialize(String username, String password) {
        this.nitriteConfig.initialized();
        this.store = nitriteConfig.getNitriteStore();
        this.store.openOrCreate(username, password, nitriteConfig.getStoreConfig());
    }
}
