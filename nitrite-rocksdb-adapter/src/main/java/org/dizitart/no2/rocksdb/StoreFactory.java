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
import org.rocksdb.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anindya Chatterjee
 */
@Slf4j(topic = "no2-rocksdb")
class StoreFactory {
    private StoreFactory() {
    }

    public static RocksDBReference createDBReference(RocksDBConfig dbConfig) {
        // create reference
        RocksDBReference reference = new RocksDBReference();

        // create options
        createOptions(reference, dbConfig);

        // create db options
        createDbOptions(reference, dbConfig);

        // create column family options
        createColumnFamilyOptions(reference, dbConfig);

        // create column family descriptors
        createColumnFamilyDescriptors(reference, dbConfig);

        // create db
        createRocksDB(reference, dbConfig);

        return reference;
    }

    private static void createOptions(RocksDBReference reference, RocksDBConfig dbConfig) {
        Options options = dbConfig.options();
        if (options == null) {
            options = new Options();
        }

        reference.setOptions(options);
    }

    private static void createDbOptions(RocksDBReference reference, RocksDBConfig dbConfig) {
        DBOptions dbOptions = dbConfig.dbOptions();
        if (dbOptions == null) {
            dbOptions = new DBOptions();
            dbOptions.setCreateIfMissing(true);
        }

        reference.setDbOptions(dbOptions);
    }

    private static void createColumnFamilyOptions(RocksDBReference reference, RocksDBConfig dbConfig) {
        ColumnFamilyOptions columnFamilyOptions = dbConfig.columnFamilyOptions();
        if (columnFamilyOptions == null) {
            columnFamilyOptions = new ColumnFamilyOptions();
            columnFamilyOptions.optimizeUniversalStyleCompaction();
        }

        reference.setColumnFamilyOptions(columnFamilyOptions);
    }

    private static void createColumnFamilyDescriptors(RocksDBReference reference, RocksDBConfig dbConfig) {
        List<ColumnFamilyDescriptor> cfDescriptors = new ArrayList<>();
        cfDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, reference.getColumnFamilyOptions()));

        // extract existing column family descriptors
        try {
            List<byte[]> columnFamilies = RocksDB.listColumnFamilies(reference.getOptions(), dbConfig.filePath());
            for (byte[] columnFamily : columnFamilies) {
                if (!Arrays.equals(RocksDB.DEFAULT_COLUMN_FAMILY, columnFamily)) {
                    cfDescriptors.add(new ColumnFamilyDescriptor(columnFamily, reference.getColumnFamilyOptions()));
                }
            }
        } catch (RocksDBException e) {
            log.error("Error while listing column families", e);
            throw new NitriteIOException("Failed to open database", e);
        }
        reference.setColumnFamilyDescriptors(cfDescriptors);
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
            throw new NitriteIOException("Failed to open database", e);
        }
    }
}
