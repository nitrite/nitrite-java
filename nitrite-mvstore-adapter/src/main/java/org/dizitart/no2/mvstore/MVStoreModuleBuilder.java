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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.dizitart.no2.store.events.StoreEventListener;
import org.h2.mvstore.FileStore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * The MVStoreModuleBuilder class is responsible for building an instance of
 * {@link MVStoreModule}. It provides methods to set various configuration
 * options for the MVStore database.
 *
 * @author Anindya Chatterjee
 * @see MVStoreModule
 * @since 4.0
 */
@Getter
@Setter
@Accessors(fluent = true)
public class MVStoreModuleBuilder {
    /**
     * The file path of the MVStore file.
     */
    private String filePath;

    /**
     * The size of the buffer used for auto-commit. When the buffer is full,
     * the changes are automatically committed to the database. The default
     * buffer size is 1024.
     */
    private int autoCommitBufferSize = 1024;

    /**
     * The encryption key to be used for encrypting the MVStore.
     */
    private char[] encryptionKey;

    /**
     * Indicates whether the MVStore instance should be opened in read-only mode.
     */
    private boolean readOnly;

    /**
     * Flag to enable/disable compression of data in MVStore.
     */
    private boolean compress;

    /**
     * Flag to enable high compression for the MVStore. If set to true, the MVStore
     * will use a higher compression level, which may result in slower read and
     * write performance but smaller file size on disk.
     */
    private boolean compressHigh;

    /**
     * Flag to enable/disable auto-commit mode. If set to true, all changes
     * will be committed immediately. If set to false, changes will be buffered
     * and committed when {@link org.dizitart.no2.Nitrite#commit()} is called.
     */
    private boolean autoCommit = true;

    /**
     * Flag to enable/disable auto-compact mode. If set to true, fragmented
     * chunks or chunks that are sufficiently below the target fill rate of 90%
     * are reclaimed. This will typically shrink the file.
     */
    private boolean autoCompact = true;

    /**
     * Indicates whether the MVStore should be opened in recovery mode or not.
     */
    private boolean recoveryMode = false;

    /**
     * The size of the read cache in MB used by the MVStore. The default value is
     * 16MB.
     */
    private int cacheSize = 16;

    /**
     * The read cache concurrency used by MVStore. Default is 16 segments.
     */
    private int cacheConcurrency = 16;

    /**
     * The amount of memory a MVStore page should contain at most, in bytes,
     * before it is split. The default is 16 KB, which is also MVStore's own
     * default for a persistent store.
     * <p>
     * MVStore reads this value in bytes. It used to default to {@code 16}, so
     * every leaf page split as soon as it held more than one entry: a store
     * carried roughly one entry per page, and every lookup descended a tree
     * about twice as deep as it needed to be.
     */
    private int pageSplitSize = 16 * 1024;

    /**
     * The file store used by the MVStore.
     */
    private FileStore<?> fileStore;

    /**
     * How long MVStore keeps a chunk after its last live page is gone, in milliseconds, before
     * its blocks may be reused. {@code null} (the default) leaves H2's own default of 45 seconds.
     * <p>
     * Earlier releases forced this to 0 together with {@link #versionsToKeep}. With both at 0 a
     * chunk's blocks can be reused while the chunk map written at close still lists that chunk,
     * and the file then fails to open with "Double mark". Setting 0 restores that behaviour and
     * is not recommended.
     */
    private Integer retentionTime;

    /**
     * How many old versions MVStore keeps. {@code null} (the default) leaves H2's own default
     * of 5. See {@link #retentionTime}.
     */
    private Integer versionsToKeep;

    /**
     * The configuration for the MVStore.
     */
    private MVStoreConfig dbConfig;

    @Setter(AccessLevel.NONE)
    /**
     * Set of event listeners to be registered with the MVStore.
     */
    private final Set<StoreEventListener> eventListeners;

    MVStoreModuleBuilder() {
        dbConfig = new MVStoreConfig();
        eventListeners = new HashSet<>();
    }

    /**
     * Sets the file path for the MVStore.
     *
     * @param file the file path for the MVStore.
     * @return the MVStoreModuleBuilder instance.
     */
    public MVStoreModuleBuilder filePath(File file) {
        if (file != null) {
            this.filePath = file.getPath();
        }
        return this;
    }

    /**
     * Sets the file path for the MVStore.
     *
     * @param path the file path for the MVStore.
     * @return the MVStoreModuleBuilder instance.
     */
    public MVStoreModuleBuilder filePath(String path) {
        this.filePath = path;
        return this;
    }

    /**
     * Adds a {@link StoreEventListener} to the module builder.
     *
     * @param listener the listener to be added
     * @return the module builder instance
     */
    public MVStoreModuleBuilder addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
        return this;
    }

    /**
     * Builds an instance of {@link MVStoreModule} with the configured parameters.
     *
     * @return an instance of {@link MVStoreModule}.
     */
    public MVStoreModule build() {
        MVStoreModule module = new MVStoreModule(filePath());

        dbConfig.filePath(filePath());
        dbConfig.autoCommitBufferSize(autoCommitBufferSize());
        dbConfig.encryptionKey(encryptionKey());
        dbConfig.isReadOnly(readOnly());
        dbConfig.compress(compress());
        dbConfig.compressHigh(compressHigh());
        dbConfig.autoCommit(autoCommit());
        dbConfig.autoCompact(autoCompact());
        dbConfig.recoveryMode(recoveryMode());
        dbConfig.cacheSize(cacheSize());
        dbConfig.cacheConcurrency(cacheConcurrency());
        dbConfig.pageSplitSize(pageSplitSize());
        dbConfig.retentionTime(retentionTime());
        dbConfig.versionsToKeep(versionsToKeep());
        dbConfig.fileStore(fileStore());
        dbConfig.eventListeners(eventListeners());

        module.setStoreConfig(dbConfig);
        return module;
    }
}
