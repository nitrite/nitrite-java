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
import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.events.StoreEventListener;
import org.h2.mvstore.FileStore;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents MV store configuration
 *
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
@Getter
@Accessors(fluent = true)
public class MVStoreConfig implements StoreConfig {
    @Setter(AccessLevel.PACKAGE)
    private Set<StoreEventListener> eventListeners;

    @Setter(AccessLevel.PACKAGE)
    private String filePath;

    @Setter(AccessLevel.PACKAGE)
    private int autoCommitBufferSize;

    @Setter(AccessLevel.PACKAGE)
    private char[] encryptionKey;

    @Setter(AccessLevel.PACKAGE)
    private Boolean isReadOnly = false;

    @Setter(AccessLevel.PACKAGE)
    private boolean compress;

    @Setter(AccessLevel.PACKAGE)
    private boolean compressHigh;

    @Setter(AccessLevel.PACKAGE)
    private boolean autoCommit;

    @Setter(AccessLevel.PACKAGE)
    private boolean autoCompact;

    @Setter(AccessLevel.PACKAGE)
    private boolean recoveryMode;

    @Setter(AccessLevel.PACKAGE)
    private int cacheSize;

    @Setter(AccessLevel.PACKAGE)
    private int cacheConcurrency;

    @Setter(AccessLevel.PACKAGE)
    private int pageSplitSize;

    @Setter(AccessLevel.PACKAGE)
    private FileStore<?> fileStore;

    MVStoreConfig() {
        eventListeners = new HashSet<>();
    }

    @Override
    public void addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
    }

    public MVStoreConfig clone() {
        MVStoreConfig config = new MVStoreConfig();
        config.eventListeners(new HashSet<>(eventListeners));
        config.filePath(filePath);
        config.autoCommitBufferSize(autoCommitBufferSize);
        config.encryptionKey(encryptionKey);
        config.isReadOnly(isReadOnly);
        config.compress(compress);
        config.compressHigh(compressHigh);
        config.autoCommit(autoCommit);
        config.autoCompact(autoCompact);
        config.recoveryMode(recoveryMode);
        config.cacheSize(cacheSize);
        config.cacheConcurrency(cacheConcurrency);
        config.pageSplitSize(pageSplitSize);
        config.fileStore(fileStore);
        return config;
    }
}
