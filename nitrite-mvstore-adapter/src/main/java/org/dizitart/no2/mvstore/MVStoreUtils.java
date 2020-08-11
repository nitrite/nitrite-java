/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
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

package org.dizitart.no2.mvstore;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.mvstore.compat.v3.MigrationUtil;
import org.dizitart.no2.store.StoreInfo;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.io.File;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @since 4.0.0
 * @author Anindya Chatterjee.
 */
@Slf4j
class MVStoreUtils {
    private MVStoreUtils() { }

    static MVStore openOrCreate(String username, String password, MVStoreConfig mvStoreConfig) {
        MVStore.Builder builder = createBuilder(mvStoreConfig);

        MVStore store = null;
        File dbFile = null;
        try {
            if (!isNullOrEmpty(mvStoreConfig.filePath())) {
                dbFile = new File(mvStoreConfig.filePath());
                if (dbFile.exists()) {
                    store = StoreFactory.openSecurely(builder, username, password);
                } else {
                    store = StoreFactory.createSecurely(builder, username, password);
                    writeStoreInfo(store);
                }
            } else {
                store = StoreFactory.createSecurely(builder, username, password);
            }
        } catch (IllegalStateException ise) {
            if (ise.getMessage().contains("file is locked")) {
                throw new NitriteIOException("database is already opened in other process");
            }

            if (!isNullOrEmpty(mvStoreConfig.filePath())) {
                try {
                    File file = new File(mvStoreConfig.filePath());
                    if (file.isDirectory()) {
                        throw new NitriteIOException(mvStoreConfig.filePath()
                            + " is a directory, must be a file");
                    }

                    if (file.exists() && file.isFile()) {
                        if (isCompatibilityError(ise)) {
                            closeStore(store);
                            store = tryMigrate(username, password, file, builder, mvStoreConfig);
                        } else {
                            log.error("Database corruption detected. Trying to repair", ise);
                            Recovery.recover(mvStoreConfig.filePath());
                            store = builder.open();
                        }
                    } else {
                        if (mvStoreConfig.isReadOnly()) {
                            throw new NitriteIOException("cannot create readonly database", ise);
                        }
                    }
                } catch (InvalidOperationException | NitriteIOException ex) {
                    throw ex;
                } catch (Exception e) {
                    throw new NitriteIOException("database file is corrupted", e);
                }
            } else {
                throw new NitriteIOException("unable to create in-memory database", ise);
            }
        } catch (IllegalArgumentException iae) {
            if (dbFile != null) {
                if (!dbFile.getParentFile().exists()) {
                    throw new NitriteIOException("directory " + dbFile.getParent() + " does not exists", iae);
                }
            }
            throw new NitriteIOException("unable to create database file", iae);
        } finally {
            if (store != null) {
                store.setRetentionTime(-1);
                store.setVersionsToKeep(2);
                store.setReuseSpace(true);
            }
        }

        return store;
    }

    static StoreInfo getStoreInfo(MVStore store) {
        if (store.hasMap(STORE_INFO)) {
            MVMap<String, Document> infoMap = store.openMap(STORE_INFO);
            Document document = infoMap.get(STORE_INFO);
            if (document != null) {
                return new StoreInfo(document);
            }
        }
        return null;
    }

    private static boolean isCompatibilityError(IllegalStateException ise) {
        return ise.getCause() != null
            && ise.getCause().getCause() instanceof ClassNotFoundException
            && ise.getCause().getCause().getMessage().contains("org.dizitart.no2");
    }

    private static void closeStore(MVStore store) {
        if (store != null && !store.isClosed()) {
            store.closeImmediately();
        }
    }

    private static void writeStoreInfo(MVStore store) {
        try {
            Document document = Document.createDocument();
            document.put(CREATE_TIME, System.currentTimeMillis());
            document.put(FILE_STORE, "MVStore/" + org.h2.engine.Constants.VERSION);
            document.put(STORE_VERSION, NITRITE_VERSION);

            MVMap<String, Document> infoMap = store.openMap(STORE_INFO);
            infoMap.put(STORE_INFO, document);
        } finally {
            store.commit();
        }
    }

    private static MVStore.Builder createBuilder(MVStoreConfig mvStoreConfig) {
        MVStore.Builder builder = new MVStore.Builder();

        if (!isNullOrEmpty(mvStoreConfig.filePath())) {
            builder = builder.fileName(mvStoreConfig.filePath());
        }

        if (!mvStoreConfig.autoCommit()) {
            builder = builder.autoCommitDisabled();
        }

        if (mvStoreConfig.autoCommitBufferSize() > 0) {
            builder = builder.autoCommitBufferSize(mvStoreConfig.autoCommitBufferSize());
        }

        // auto compact disabled github issue #41
        builder.autoCompactFillRate(0);

        if (mvStoreConfig.encryptionKey() != null) {
            builder = builder.encryptionKey(mvStoreConfig.encryptionKey());
        }

        if (mvStoreConfig.isReadOnly()) {
            if (isNullOrEmpty(mvStoreConfig.filePath())) {
                throw new InvalidOperationException("unable create readonly in-memory database");
            }
            builder = builder.readOnly();
        }

        if (mvStoreConfig.recoveryMode()) {
            builder = builder.recoveryMode();
        }

        if (mvStoreConfig.cacheSize() > 0) {
            builder = builder.cacheSize(mvStoreConfig.cacheSize());
        }

        if (mvStoreConfig.cacheConcurrency() > 0) {
            builder = builder.cacheConcurrency(mvStoreConfig.cacheConcurrency());
        }

        if (mvStoreConfig.compress()) {
            builder = builder.compress();
        }

        if (mvStoreConfig.compressHigh()) {
            builder = builder.compressHigh();
        }

        if (mvStoreConfig.pageSplitSize() > 0) {
            builder = builder.pageSplitSize(mvStoreConfig.pageSplitSize());
        }

        if (isNullOrEmpty(mvStoreConfig.filePath()) && mvStoreConfig.fileStore() != null) {
            // for in-memory store use off-heap storage
            builder = builder.fileStore(mvStoreConfig.fileStore());
        }

        return builder;
    }

    private static MVStore tryMigrate(String username, String password, File orgFile,
                               MVStore.Builder builder, MVStoreConfig storeConfig) {
        log.info("Migrating old database format to new database format");

        // open old store with builder
        MVStore oldMvStore = builder.open();

        // create new store with builder
        File newFile = new File(orgFile.getPath() + "_new");
        storeConfig.filePath(newFile.getPath());
        MVStore.Builder newBuilder = createBuilder(storeConfig);
        MVStore newMvStore = newBuilder.open();

        // migrate 2 stores maps
        MigrationUtil.migrate(newMvStore, oldMvStore);
        switchFiles(newFile, orgFile);

        // open new store calling openOrCreate and return
        storeConfig.filePath(orgFile.getPath());
        MVStore store = openOrCreate(username, password, storeConfig);
        writeStoreInfo(store);

        return store;
    }

    private static void switchFiles(File newFile, File orgFile) {
        File backupFile = new File(orgFile.getPath() + "_old");
        if (orgFile.renameTo(backupFile)) {
            if (!newFile.renameTo(orgFile)) {
                throw new NitriteIOException("could not rename new data file");
            }

            if (!backupFile.delete()) {
                throw new NitriteIOException("could not delete backup data file");
            }
        } else {
            throw new NitriteIOException("could not create backup copy of old data file");
        }
    }
}
