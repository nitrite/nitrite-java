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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.exceptions.NitriteException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NitriteSecurityException;
import org.dizitart.no2.migration.MigrationManager;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.RepositoryFactory;
import org.dizitart.no2.repository.EntityDecorator;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreMetaData;
import org.dizitart.no2.store.UserAuthenticationService;
import org.dizitart.no2.transaction.Session;

import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.NITRITE_VERSION;
import static org.dizitart.no2.common.Constants.STORE_INFO;
import static org.dizitart.no2.common.util.ObjectUtils.findRepositoryName;
import static org.dizitart.no2.common.util.ObjectUtils.findRepositoryNameByDecorator;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee.
 * @since 4.0
 */
@Slf4j(topic = "nitrite")
class NitriteDatabase implements Nitrite {
    private final CollectionFactory collectionFactory;
    private final RepositoryFactory repositoryFactory;
    private final NitriteConfig nitriteConfig;
    private final LockService lockService;
    private NitriteMap<String, Document> storeInfo;
    private NitriteStore<?> store;

    NitriteDatabase(NitriteConfig config) {
        this.nitriteConfig = config;
        this.lockService = new LockService();
        this.collectionFactory = new CollectionFactory(lockService);
        this.repositoryFactory = new RepositoryFactory(collectionFactory);
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
    public <T> ObjectRepository<T> getRepository(EntityDecorator<T> entityDecorator) {
        checkOpened();
        return repositoryFactory.getRepository(nitriteConfig, entityDecorator);
    }

    @Override
    public <T> ObjectRepository<T> getRepository(EntityDecorator<T> entityDecorator, String key) {
        checkOpened();
        return repositoryFactory.getRepository(nitriteConfig, entityDecorator, key);
    }

    @Override
    public void destroyCollection(String name) {
        checkOpened();
        store.removeMap(name);
    }

    @Override
    public <T> void destroyRepository(Class<T> type) {
        checkOpened();
        String mapName = findRepositoryName(type, null);
        store.removeMap(mapName);
    }

    @Override
    public <T> void destroyRepository(Class<T> type, String key) {
        checkOpened();
        String mapName = findRepositoryName(type, key);
        store.removeMap(mapName);
    }

    @Override
    public <T> void destroyRepository(EntityDecorator<T> type) {
        checkOpened();
        String mapName = findRepositoryNameByDecorator(type, null);
        store.removeMap(mapName);
    }

    @Override
    public <T> void destroyRepository(EntityDecorator<T> type, String key) {
        checkOpened();
        String mapName = findRepositoryNameByDecorator(type, key);
        store.removeMap(mapName);
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
    public Map<String, Set<String>> listKeyedRepositories() {
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
    public NitriteStore<?> getStore() {
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
            storeInfo.close();

            if (nitriteConfig != null) {
                // close all plugins and store
                nitriteConfig.close();
            }

            log.info("Nitrite database has been closed successfully.");
        } catch (NitriteIOException e) {
            throw e;
        } catch (Throwable error) {
            throw new NitriteIOException("Error occurred while closing the database", error);
        }
    }

    @Override
    public void commit() {
        checkOpened();
        if (store != null) {
            try {
                store.commit();
            } catch (Exception e) {
                throw new NitriteIOException("Error occurred while committing the database", e);
            }
            log.debug("Unsaved changes has been committed successfully.");
        }
    }

    @Override
    public StoreMetaData getDatabaseMetaData() {
        Document document = storeInfo.get(STORE_INFO);
        if (document == null) {
            prepareDatabaseMetaData();
            document = storeInfo.get(STORE_INFO);
        }
        return new StoreMetaData(document);
    }

    @Override
    public Session createSession() {
        return new Session(this, lockService);
    }

    public void initialize(String username, String password) {
        validateUserCredentials(username, password);
        try {
            nitriteConfig.initialize();
            store = nitriteConfig.getNitriteStore();

            store.openOrCreate();
            prepareDatabaseMetaData();

            MigrationManager migrationManager = new MigrationManager(this);
            migrationManager.doMigrate();

            UserAuthenticationService userAuthenticationService = new UserAuthenticationService(store);
            userAuthenticationService.authenticate(username, password);
        } catch (Exception e) {
            log.error("Error occurred while initializing the database", e);
            if (store != null && !store.isClosed()) {
                try {
                    store.close();
                } catch (Exception ex) {
                    log.error("Error occurred while closing the database");
                    throw new NitriteIOException("Failed to close database", ex);
                }
            }
            if (e instanceof NitriteException) {
                throw e;
            } else {
                throw new NitriteIOException("Failed to initialize database", e);
            }
        }
    }

    private void validateUserCredentials(String username, String password) {
        if (isNullOrEmpty(username) && isNullOrEmpty(password)) {
            return;
        }

        if (isNullOrEmpty(username)) {
            throw new NitriteSecurityException("Username is required");
        }
        if (isNullOrEmpty(password)) {
            throw new NitriteSecurityException("Password is required");
        }
    }

    private void prepareDatabaseMetaData() {
        storeInfo = this.store.openMap(STORE_INFO, String.class, Document.class);

        if (storeInfo.isEmpty()) {
            StoreMetaData storeMetadata = new StoreMetaData();
            storeMetadata.setCreateTime(System.currentTimeMillis());
            storeMetadata.setStoreVersion(store.getStoreVersion());
            storeMetadata.setNitriteVersion(NITRITE_VERSION);
            storeMetadata.setSchemaVersion(nitriteConfig.getSchemaVersion());

            storeInfo.put(STORE_INFO, storeMetadata.getInfo());
        }
    }
}
