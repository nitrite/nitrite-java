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
 * Configuration class for MVStore.
 * 
 * @since 4.0
 * @author Anindya Chatterjee
 */
@Getter
@Accessors(fluent = true)
public class MVStoreConfig implements StoreConfig {
    @Setter(AccessLevel.PACKAGE)
    /**
     * The set of event listeners for the MVStore.
     */
    private Set<StoreEventListener> eventListeners;

    @Setter(AccessLevel.PACKAGE)
    /**
     * The file path of the MVStore file.
     */
    private String filePath;

    @Setter(AccessLevel.PACKAGE)
    /**
     * The size of the buffer used for auto-commit operations.
     */
    private int autoCommitBufferSize;

    @Setter(AccessLevel.PACKAGE)
    /**
     * The encryption key to be used for encrypting and decrypting the data.
     */
    private char[] encryptionKey;

    @Setter(AccessLevel.PACKAGE)
    /**
     * A flag indicating whether the MVStore should be opened in read-only mode.
     */
    private Boolean isReadOnly = false;

    @Setter(AccessLevel.PACKAGE)
    /**
     * Indicates whether the MVStore should compress data or not.
     */
    private boolean compress;

    @Setter(AccessLevel.PACKAGE)
    /**
     * Indicates whether to use high compression for data blocks.
     */
    private boolean compressHigh;

    @Setter(AccessLevel.PACKAGE)
    /**
     * Indicates whether auto-commit mode is enabled for the MVStore.
     */
    private boolean autoCommit;

    @Setter(AccessLevel.PACKAGE)
    /**
     * Sets a value indicating whether the MVStore should automatically compact
     * itself when it is closed.
     */
    private boolean autoCompact;

    @Setter(AccessLevel.PACKAGE)
    /**
     * Indicates whether the MVStore should be opened in recovery mode or not.
     */
    private boolean recoveryMode;

    @Setter(AccessLevel.PACKAGE)
    /**
     * The size of the cache (in KB) used by the MVStore.
     */
    private int cacheSize;

    @Setter(AccessLevel.PACKAGE)
    /**
     * The number of threads that can concurrently access the MVStore cache.
     */
    private int cacheConcurrency;

    /**
     * Sets the page split size for the MVStore.
     */
    @Setter(AccessLevel.PACKAGE)
    private int pageSplitSize;

    @Setter(AccessLevel.PACKAGE)
    /**
     * The file store used by the MVStore.
     */
    private FileStore<?> fileStore;

    MVStoreConfig() {
        eventListeners = new HashSet<>();
    }

    @Override
    public void addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     */
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
