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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dizitart.no2.rocksdb.serializers.ObjectSerializer;
import org.dizitart.no2.store.events.StoreEventListener;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * A builder class to create a RocksDBModule instance with the desired configuration.
 * 
 * @since 4.0
 * @see RocksDBModule
 * @author Anindya Chatterjee
 */
@Getter
@Setter
@Accessors(fluent = true)
public class RocksDBModuleBuilder {
    /**
     * The file path of the RocksDB data store.
     */
    private String filePath;
    /**
     * The RocksDB {@link Options} used by the module builder.
     */
    private Options options;
    /**
     * The RocksDB {@link DBOptions} used by the module builder.
     */
    private DBOptions dbOptions;
    /**
     * The RocksDB {@link ColumnFamilyOptions} used by the module builder.
     */
    private ColumnFamilyOptions columnFamilyOptions;
    /**
     * The object formatter used to serialize and deserialize objects.
     */
    private ObjectSerializer objectSerializer;
    /**
     * The RocksDB configuration for the module.
     */
    private RocksDBConfig dbConfig;

    @Setter(AccessLevel.NONE)
    private final Set<StoreEventListener> eventListeners;

    RocksDBModuleBuilder() {
        dbConfig = new RocksDBConfig();
        eventListeners = new HashSet<>();
    }

    /**
     * Sets the file path for the RocksDB data store.
     *
     * @param file the file path for the RocksDB data store.
     * @return the {@link RocksDBModuleBuilder} instance.
     */
    public RocksDBModuleBuilder filePath(File file) {
        if (file != null) {
            this.filePath = file.getPath();
        }
        return this;
    }

    /**
     * Sets the file path for the RocksDB data store.
     *
     * @param path the file path for the RocksDB data store.
     * @return the current {@link RocksDBModuleBuilder} instance.
     */
    public RocksDBModuleBuilder filePath(String path) {
        this.filePath = path;
        return this;
    }

    /**
     * Adds a {@link StoreEventListener} to the module builder.
     *
     * @param listener the listener to add
     * @return the {@link RocksDBModuleBuilder} instance
     */
    public RocksDBModuleBuilder addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
        return this;
    }

    /**
     * Builds a {@link RocksDBModule} with the specified configuration.
     *
     * @return the {@link RocksDBModule} instance.
     */
    public RocksDBModule build() {
        RocksDBModule module = new RocksDBModule(filePath());

        dbConfig.options(options());
        dbConfig.dbOptions(dbOptions());
        dbConfig.columnFamilyOptions(columnFamilyOptions());
        dbConfig.filePath(filePath());

        if (objectSerializer() != null) {
            dbConfig.objectSerializer(objectSerializer());
        }
        dbConfig.eventListeners(eventListeners());

        module.setStoreConfig(dbConfig);
        return module;
    }
}
