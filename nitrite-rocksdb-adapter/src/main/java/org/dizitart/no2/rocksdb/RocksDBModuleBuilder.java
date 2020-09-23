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
import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.dizitart.no2.store.events.StoreEventListener;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Getter
@Setter
@Accessors(fluent = true)
public class RocksDBModuleBuilder {
    private boolean createIfMissing = true;
    private boolean errorIfExists;
    private int writeBufferSize = 4 << 20;
    private int maxOpenFiles = 1000;
    private boolean paranoidChecks;
    private String filePath;
    private ObjectFormatter objectFormatter;
    private RocksDBConfig dbConfig;

    @Setter(AccessLevel.NONE)
    private final Set<StoreEventListener> eventListeners;

    RocksDBModuleBuilder() {
        dbConfig = new RocksDBConfig();
        eventListeners = new HashSet<>();
    }

    public RocksDBModuleBuilder filePath(File file) {
        if (file != null) {
            this.filePath = file.getPath();
        }
        return this;
    }

    public RocksDBModuleBuilder filePath(String path) {
        this.filePath = path;
        return this;
    }

    public RocksDBModuleBuilder addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
        return this;
    }

    public RocksDBModule build() {
        RocksDBModule module = new RocksDBModule(filePath());

        dbConfig.createIfMissing(createIfMissing());
        dbConfig.errorIfExists(errorIfExists());
        dbConfig.writeBufferSize(writeBufferSize());
        dbConfig.maxOpenFiles(maxOpenFiles());
        dbConfig.paranoidChecks(paranoidChecks());
        dbConfig.filePath(filePath());

        if (objectFormatter() != null) {
            dbConfig.objectFormatter(objectFormatter());
        }
        dbConfig.eventListeners(eventListeners());

        module.setStoreConfig(dbConfig);
        return module;
    }
}
