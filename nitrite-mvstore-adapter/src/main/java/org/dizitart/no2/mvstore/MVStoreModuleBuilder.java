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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dizitart.no2.store.events.StoreEventListener;
import org.h2.mvstore.FileStore;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Getter
@Setter
@Accessors(fluent = true)
public class MVStoreModuleBuilder {
    private String filePath;
    private int autoCommitBufferSize = 1024;
    private char[] encryptionKey;
    private boolean readOnly;
    private boolean compress;
    private boolean compressHigh;
    private boolean autoCommit = true;
    private boolean recoveryMode = false;
    private int cacheSize = 16;
    private int cacheConcurrency = 16;
    private int pageSplitSize = 16;
    private FileStore fileStore;
    private MVStoreConfig dbConfig;

    @Setter(AccessLevel.NONE)
    private final Set<StoreEventListener> eventListeners;

    MVStoreModuleBuilder() {
        dbConfig = new MVStoreConfig();
        eventListeners = new HashSet<>();
    }

    public MVStoreModuleBuilder filePath(File file) {
        if (file != null) {
            this.filePath = file.getPath();
        }
        return this;
    }

    public MVStoreModuleBuilder filePath(String path) {
        this.filePath = path;
        return this;
    }

    public MVStoreModuleBuilder addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
        return this;
    }

    public MVStoreModule build() {
        MVStoreModule module = new MVStoreModule();

        dbConfig.filePath(filePath());
        dbConfig.autoCommitBufferSize(autoCommitBufferSize());
        dbConfig.encryptionKey(encryptionKey());
        dbConfig.isReadOnly(readOnly());
        dbConfig.compress(compress());
        dbConfig.compressHigh(compressHigh());
        dbConfig.autoCommit(autoCommit());
        dbConfig.recoveryMode(recoveryMode());
        dbConfig.cacheSize(cacheSize());
        dbConfig.cacheConcurrency(cacheConcurrency());
        dbConfig.pageSplitSize(pageSplitSize());
        dbConfig.fileStore(fileStore());
        dbConfig.eventListeners(eventListeners());

        module.setStoreConfig(dbConfig);
        return module;
    }
}
