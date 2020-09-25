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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.rocksdb.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anindya Chatterjee
 */
@Data
@Slf4j
public class RocksDBReference implements AutoCloseable {
    private Options options;
    private DBOptions dbOptions;
    private ColumnFamilyOptions columnFamilyOptions;
    private RocksDB rocksDB;

    private List<ColumnFamilyDescriptor> columnFamilyDescriptors;
    private Map<String, ColumnFamilyHandle> columnFamilyHandleRegistry;
    private List<AbstractComparator> dbComparators;

    public RocksDBReference() {
        this.columnFamilyDescriptors = new ArrayList<>();
        this.columnFamilyHandleRegistry = new ConcurrentHashMap<>();
        this.dbComparators = new ArrayList<>();
    }

    @Override
    public void close() throws RocksDBException {
        columnFamilyHandleRegistry.values().forEach(AbstractImmutableNativeReference::close);
        columnFamilyHandleRegistry.clear();

        rocksDB.closeE();
        dbOptions.close();
        dbComparators.forEach(AbstractImmutableNativeReference::close);
        columnFamilyOptions.close();
        options.close();
    }

    public synchronized ColumnFamilyHandle getOrCreateColumnFamily(String name) {
        if (columnFamilyHandleRegistry.containsKey(name)) {
            return columnFamilyHandleRegistry.get(name);
        } else {
            try {
                ColumnFamilyHandle handle = rocksDB.createColumnFamily(
                    new ColumnFamilyDescriptor(name.getBytes(StandardCharsets.UTF_8), columnFamilyOptions));
                columnFamilyHandleRegistry.put(name, handle);
                return handle;
            } catch (RocksDBException e) {
                log.error("Error while retrieving column family handle", e);
                throw new NitriteIOException("failed to obtain column family handle", e);
            }
        }
    }

    public void dropColumnFamily(String mapName) {
        if (columnFamilyHandleRegistry.containsKey(mapName)) {
            try {
                ColumnFamilyHandle handle = columnFamilyHandleRegistry.get(mapName);
                rocksDB.dropColumnFamily(handle);
                handle.close();
                columnFamilyHandleRegistry.remove(mapName);
            } catch (RocksDBException e) {
                log.error("Error while dropping column family " + mapName, e);
                throw new NitriteIOException("failed to drop column family", e);
            }
        }
    }

    public void addComparator(AbstractComparator comparator) {
        dbComparators.add(comparator);
    }
}
