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
import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.migration.MigrationManager;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.RepositoryFactory;
import org.dizitart.no2.store.DatabaseMetaData;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.UserAuthenticationService;
import org.dizitart.no2.transaction.Session;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.NITRITE_VERSION;
import static org.dizitart.no2.common.Constants.STORE_INFO;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee.
 * @since 4.0
 */
@Slf4j
class NitriteDatabase implements Nitrite {
    private final CollectionFactory collectionFactory;
    private final RepositoryFactory repositoryFactory;
    private final NitriteConfig nitriteConfig;
    private final LockService lockService;
    private NitriteStore<?> store;

    NitriteDatabase(NitriteConfig config) {
        this.nitriteConfig = config;
        this.lockService = new LockService();
        this.collectionFactory = new CollectionFactory(lockService);
        this.repositoryFactory = new RepositoryFactory(collectionFactory);
        this.initialize(null, null);
    }

    NitriteDatabase(String username, String password, NitriteConfig config) {
        validateUserCredentials(username, password);
        this.nitriteConfig = config;
        this.lockService = new LockService();
        this.collectionFactory = new CollectionFactory(lockService);
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
            store.close();
            log.info("Nitrite database has been closed successfully.");
        } catch (NitriteIOException e) {
            throw e;
        } catch (Throwable error) {
            throw new NitriteIOException("error while shutting down nitrite", error);
        }
    }

    @Override
    public void commit() {
        checkOpened();
        if (store != null) {
            try {
                store.commit();
            } catch (Exception e) {
                throw new NitriteIOException("failed to commit changes", e);
            }
            log.debug("Unsaved changes committed successfully.");
        }
    }

    @Override
    public DatabaseMetaData getDatabaseMetaData() {
        NitriteMap<String, Document> storeInfo = this.store.openMap(STORE_INFO,
            String.class, Document.class);

        Document document = storeInfo.get(STORE_INFO);
        if (document == null) {
            prepareDatabaseMetaData();
            document = storeInfo.get(STORE_INFO);
        }
        return new DatabaseMetaData(document);
    }

    @Override
    public Session createSession() {
        return new Session(this, lockService);
    }

    private void validateUserCredentials(String username, String password) {
        if (isNullOrEmpty(username)) {
            throw new SecurityException("username cannot be empty");
        }
        if (isNullOrEmpty(password)) {
            throw new SecurityException("password cannot be empty");
        }
    }

    private void initialize(String username, String password) {
        try {
            nitriteConfig.initialize();
            store = nitriteConfig.getNitriteStore();
            boolean isExisting = isExisting();

            store.openOrCreate();
            prepareDatabaseMetaData();

            MigrationManager migrationManager = new MigrationManager(this);
            migrationManager.doMigrate();

            UserAuthenticationService userAuthenticationService = new UserAuthenticationService(store);
            userAuthenticationService.authenticate(username, password, isExisting);
        } catch (NitriteException e) {
            log.error("Error while initializing the database", e);
            if (store != null && !store.isClosed()) {
                try {
                    store.close();
                } catch (Exception ex) {
                    log.error("Error while closing the database", ex);
                    throw new NitriteIOException("failed to close database", ex);
                }
            }
            throw e;
        } catch (Exception e) {
            log.error("Error while initializing the database", e);
            if (store != null && !store.isClosed()) {
                try {
                    store.close();
                } catch (Exception ex) {
                    log.error("Error while closing the database");
                    throw new NitriteIOException("failed to close database", ex);
                }
            }
            throw new NitriteIOException("failed to initialize database", e);
        }
    }

    private void prepareDatabaseMetaData() {
        NitriteMap<String, Document> storeInfo = this.store.openMap(STORE_INFO,
            String.class, Document.class);

        if (storeInfo.isEmpty()) {
            DatabaseMetaData databaseMetadata = new DatabaseMetaData();
            databaseMetadata.setCreateTime(System.currentTimeMillis());
            databaseMetadata.setStoreVersion(store.getStoreVersion());
            databaseMetadata.setNitriteVersion(NITRITE_VERSION);
            databaseMetadata.setSchemaVersion(nitriteConfig.getSchemaVersion());

            storeInfo.put(STORE_INFO, databaseMetadata.getInfo());
        }
    }

    private boolean isExisting() {
        String filePath = store.getStoreConfig().filePath();
        if (!isNullOrEmpty(filePath)) {
            File dbFile = new File(filePath);
            return dbFile.exists();
        }
        return false;
    }
}
