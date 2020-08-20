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

package org.dizitart.no2.rocksdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.dizitart.no2.store.UserCredential;
import org.rocksdb.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.dizitart.no2.common.Constants.USER_MAP;
import static org.dizitart.no2.common.util.Security.*;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 */
@Slf4j(topic = "no2-rocksdb")
class StoreFactory {

    private StoreFactory() {
    }

    public static RocksDBReference createSecurely(RocksDBConfig dbConfig,
                                                  String userId,
                                                  String password) {
        RocksDBReference reference = createDBReference(dbConfig);
        ObjectFormatter objectFormatter = dbConfig.objectFormatter();

        try {
            ColumnFamilyHandle userMap = reference.getOrCreateColumnFamily(USER_MAP);
            if (!isNullOrEmpty(password) && !isNullOrEmpty(userId)) {
                byte[] salt = getNextSalt();
                byte[] hash = hash(password.toCharArray(), salt);

                UserCredential userCredential = new UserCredential();
                userCredential.setPasswordHash(hash);
                userCredential.setPasswordSalt(salt);

                byte[] key = objectFormatter.encode(userId);
                byte[] value = objectFormatter.encode(userCredential);

                reference.getRocksDB().put(userMap, key, value);
            }
        } catch (RocksDBException e) {
            log.error("Error while creating database", e);
            throw new NitriteIOException("failed to create database", e);
        }

        return reference;
    }

    public static RocksDBReference openSecurely(RocksDBConfig dbConfig,
                                                String userId,
                                                String password) {
        RocksDBReference reference = createDBReference(dbConfig);
        ObjectFormatter objectFormatter = dbConfig.objectFormatter();
        boolean success = false;

        try {
            ColumnFamilyHandle userMap = reference.getOrCreateColumnFamily(USER_MAP);
            if (!isNullOrEmpty(password) && !isNullOrEmpty(userId)) {
                try (RocksIterator iterator = reference.getRocksDB().newIterator(userMap)) {
                    iterator.seekToFirst();
                    if (!iterator.isValid()) {
                        throw new SecurityException("no user map found in the database");
                    }
                }

                try {
                    byte[] key = objectFormatter.encode(userId);
                    byte[] value = reference.getRocksDB().get(userMap, key);
                    if (value == null) {
                        throw new SecurityException("username or password is invalid");
                    }

                    UserCredential userCredential = objectFormatter.decode(value, UserCredential.class);
                    if (userCredential != null) {
                        byte[] salt = userCredential.getPasswordSalt();
                        byte[] expectedHash = userCredential.getPasswordHash();

                        if (!isExpectedPassword(password.toCharArray(), salt, expectedHash)) {
                            throw new SecurityException("username or password is invalid");
                        }
                    } else {
                        throw new SecurityException("username or password is invalid");
                    }

                } catch (RocksDBException e) {
                    log.error("Error while opening database", e);
                    throw new NitriteIOException("failed to open database", e);
                }
            } else {
                try (RocksIterator iterator = reference.getRocksDB().newIterator(userMap)) {
                    iterator.seekToFirst();
                    if (iterator.isValid()) {
                        throw new SecurityException("user map found unexpectedly");
                    }
                }
            }

            success = true;
            return reference;
        } finally {
            if (!success) {
                try {
                    reference.close();
                } catch (RocksDBException e) {
                    log.error("Error while closing database", e);
                }
            }
        }
    }


    private static RocksDBReference createDBReference(RocksDBConfig dbConfig) {
        // create reference
        RocksDBReference reference = new RocksDBReference();

        // create column family options
        createColumnFamilyOptions(reference, dbConfig);

        // create column family descriptors
        createColumnFamilyDescriptors(reference, dbConfig);

        // create db options
        createDbOptions(reference, dbConfig);

        // create db
        createRocksDB(reference, dbConfig);

        return reference;
    }

    private static void createColumnFamilyOptions(RocksDBReference reference, RocksDBConfig dbConfig) {
        ColumnFamilyOptions cfOpts = new ColumnFamilyOptions()
//            .optimizeForSmallDb()
            .optimizeUniversalStyleCompaction();

//        ComparatorOptions comparatorOptions = new ComparatorOptions();

        // set custom comparator
//        AbstractComparator comparator = new NitriteRocksDBComparator(comparatorOptions, dbConfig.marshaller());

//        cfOpts.setComparator(comparator);
//        reference.setDbComparator(comparator);
//        reference.setComparatorOptions(comparatorOptions);
        reference.setColumnFamilyOptions(cfOpts);
    }

    private static void createColumnFamilyDescriptors(RocksDBReference reference, RocksDBConfig dbConfig) {
        List<ColumnFamilyDescriptor> cfDescriptors = new ArrayList<>();
        cfDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, reference.getColumnFamilyOptions()));

        // extract existing column family descriptors
        try {
            List<byte[]> columnFamilies = RocksDB.listColumnFamilies(new Options(), dbConfig.filePath());
            for (byte[] columnFamily : columnFamilies) {
                if (!Arrays.equals(RocksDB.DEFAULT_COLUMN_FAMILY, columnFamily)) {
                    cfDescriptors.add(new ColumnFamilyDescriptor(columnFamily, reference.getColumnFamilyOptions()));
                }
            }
        } catch (RocksDBException e) {
            log.error("Error while listing column families", e);
            throw new NitriteIOException("failed to open database", e);
        }
        reference.setColumnFamilyDescriptors(cfDescriptors);
    }

    private static void createDbOptions(RocksDBReference reference, RocksDBConfig dbConfig) {
        DBOptions options = new DBOptions();
        options.setCreateIfMissing(dbConfig.createIfMissing());
//        options.setErrorIfExists(dbConfig.errorIfExists());
//        options.setDbWriteBufferSize(dbConfig.writeBufferSize());
//        options.setMaxOpenFiles(dbConfig.maxOpenFiles());
//        options.setParanoidChecks(dbConfig.paranoidChecks());

        reference.setDbOptions(options);
    }

    private static void createRocksDB(RocksDBReference reference, RocksDBConfig dbConfig) {
        try {
            List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();
            RocksDB db = RocksDB.open(reference.getDbOptions(), dbConfig.filePath(),
                reference.getColumnFamilyDescriptors(), columnFamilyHandleList);
            reference.setRocksDB(db);

            Map<String, ColumnFamilyHandle> handleMap = new ConcurrentHashMap<>();
            for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandleList) {
                String name = new String(columnFamilyHandle.getName(), StandardCharsets.UTF_8);
                handleMap.put(name, columnFamilyHandle);
            }
            reference.setColumnFamilyHandleRegistry(handleMap);
        } catch (RocksDBException e) {
            log.error("Error while opening rocks database", e);
            throw new NitriteIOException("failed to open database", e);
        }
    }
}
