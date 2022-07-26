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
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.mvstore.compat.v1.UpgradeUtil;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreException;

import java.io.File;

import static org.dizitart.no2.common.Constants.META_MAP_NAME;
import static org.dizitart.no2.common.Constants.STORE_INFO;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
@Slf4j
class MVStoreUtils {
    private MVStoreUtils() {
    }

    static MVStore openOrCreate(MVStoreConfig storeConfig) {
        MVStore.Builder builder = createBuilder(storeConfig);

        MVStore store = null;
        File dbFile = !isNullOrEmpty(storeConfig.filePath()) ? new File(storeConfig.filePath()) : null;
        try {
            store = builder.open();
            testForMigration(store);
        } catch (MVStoreException me) {
            if (me.getMessage().contains("file is locked")) {
                throw new NitriteIOException("Database is already opened in other process");
            }

            if (dbFile != null) {
                try {
                    if (dbFile.isDirectory()) {
                        throw new NitriteIOException(storeConfig.filePath() + " is a directory, must be a file");
                    }

                    if (dbFile.exists() && dbFile.isFile()) {
                        if (isCompatibilityError(me)) {
                            if (store != null) {
                                store.closeImmediately();
                            }

                            // try upgrading the database
                            store = tryUpgrade(dbFile, storeConfig);
                        } else {
                            log.error("Database corruption detected. Trying to repair", me);
                            Recovery.recover(storeConfig.filePath());
                            store = builder.open();
                        }
                    } else {
                        if (storeConfig.isReadOnly()) {
                            throw new NitriteIOException("Cannot create readonly database", me);
                        }
                    }
                } catch (InvalidOperationException | NitriteIOException ex) {
                    throw ex;
                } catch (Exception e) {
                    throw new NitriteIOException("Database file is corrupted", e);
                }
            } else {
                throw new NitriteIOException("Unable to create in-memory database", me);
            }
        } catch (IllegalArgumentException iae) {
            if (dbFile != null) {
                if (!dbFile.getParentFile().exists()) {
                    throw new NitriteIOException("Directory " + dbFile.getParent() + " does not exists", iae);
                }
            }
            throw new NitriteIOException("Unable to create database file", iae);
        } finally {
            if (store != null) {
                store.setRetentionTime(0);
                store.setVersionsToKeep(0);
                store.setReuseSpace(true);
            }
        }

        return store;
    }

    private static boolean isCompatibilityError(Exception e) {
        return e.getMessage().contains("The write format 1 is smaller than the supported format");
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
                throw new InvalidOperationException("Unable create readonly in-memory database");
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

    private static MVStore tryUpgrade(File orgFile, MVStoreConfig storeConfig) {
        // create new store with builder
        File newFile = new File(orgFile.getPath() + "_new");
        MVStoreConfig newStoreConfig = storeConfig.clone();
        newStoreConfig.filePath(newFile.getPath());
        MVStore.Builder newBuilder = createBuilder(newStoreConfig);

        UpgradeUtil.tryUpgrade(newBuilder, storeConfig);

        // switch the file
        switchFiles(newFile, orgFile);
        return openOrCreate(storeConfig);
    }

    private static void switchFiles(File newFile, File orgFile) {
        File backupFile = new File(orgFile.getPath() + "_old");
        if (orgFile.renameTo(backupFile)) {
            if (!newFile.renameTo(orgFile)) {
                throw new NitriteIOException("Could not rename new data file");
            }

            if (!backupFile.delete()) {
                throw new NitriteIOException("Could not delete backup data file");
            }
        } else {
            throw new NitriteIOException("Could not create backup copy of old data file");
        }
    }

    private static void testForMigration(MVStore store) {
        if (store != null) {
            if (store.hasMap(STORE_INFO)) {
                return;
            }

            MVStore.TxCounter txCounter = store.registerVersionUsage();
            MVMap<String, Attributes> metaMap = store.openMap(META_MAP_NAME);
            try {
                // fire one operation to trigger compatibility issue
                // if no exception thrown, then the database is compatible
                metaMap.remove("MigrationTest");
            } catch (IllegalStateException e) {
                store.close();
                throw e;
            } finally {
                if (!store.isClosed()) {
                    store.deregisterVersionUsage(txCounter);
                }
            }
        }
    }
}
