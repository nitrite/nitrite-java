/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.store;


import lombok.AccessLevel;
import lombok.Getter;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.store.events.StoreEventListener;
import org.h2.mvstore.FileStore;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents MV store configuration
 *
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
@Getter
public class MVStoreConfig implements StoreConfig {
    @Getter(AccessLevel.PACKAGE)
    private final Set<StoreEventListener> eventListeners;
    private String filePath;
    private int autoCommitBufferSize;
    private boolean readOnly;
    private boolean compressed;
    private boolean autoCommit = true;
    private boolean autoCompact = true;
    private FileStore fileStore;
    @Getter(AccessLevel.NONE)
    private boolean configured = false;

    private MVStoreConfig() {
        eventListeners = new HashSet<>();
    }

    /**
     * Creates a mew {@link MVStoreConfig} instance.
     *
     * @return a new {@link MVStoreConfig} instance.
     */
    public static MVStoreConfig create() {
        return new MVStoreConfig();
    }

    /**
     * Sets file name for the file based store. If `file` is `null`
     * the builder will create an in-memory database.
     *
     * @param path the name of the file store.
     */
    public void filePath(String path) {
        if (configured) {
            throw new InvalidOperationException("cannot change the path after database" +
                " initialization");
        }
        this.filePath = path;
    }

    /**
     * Sets file name for the file based store. If `file` is `null`
     * the builder will create an in-memory database.
     *
     * @param file the name of the file store.
     */
    public void filePath(File file) {
        if (configured) {
            throw new InvalidOperationException("cannot change the file path after database" +
                " initialization");
        }
        if (file == null) {
            this.filePath = null;
        } else {
            this.filePath = file.getPath();
        }
    }

    /**
     * Sets {@link FileStore} for mv store. If `file` is `null`
     * the builder will create an in-memory database.
     *
     * @param fileStore the {@link FileStore} instance.
     */
    public void fileStore(FileStore fileStore) {
        if (configured) {
            throw new InvalidOperationException("cannot change the file store after database" +
                " initialization");
        }
        this.fileStore = fileStore;
    }

    /**
     * Sets the size of the write buffer, in KB disk space (for file-based
     * stores). Unless auto-commit is disabled, changes are automatically
     * saved if there are more than this amount of changes.
     * <p>
     * When the values is set to 0 or lower, it will assume the default value
     * - 1024 KB.
     * </p>
     *
     * <b>NOTE:</b> If auto commit is disabled by {@link MVStoreConfig#disableAutoCommit()},
     * then buffer size has no effect.
     *
     * @param size the buffer size in KB
     */
    public void autoCommitBufferSize(int size) {
        if (configured) {
            throw new InvalidOperationException("cannot change buffer size after database" +
                " initialization");
        }
        this.autoCommitBufferSize = size;
    }

    /**
     * Opens the file in read-only mode. In this case, a shared lock will be
     * acquired to ensure the file is not concurrently opened in write mode.
     * <p>
     * If this option is not used, the file is locked exclusively.
     * </p>
     *
     * <b>NOTE:</b> A file store may only be opened once in every JVM (no matter
     * whether it is opened in read-only or read-write mode), because each
     * file may be locked only once in a process.
     */
    public void readOnly() {
        if (configured) {
            throw new InvalidOperationException("cannot change readonly property after database" +
                " initialization");
        }
        this.readOnly = true;
    }

    /**
     * Compresses data before writing using the LZF algorithm. This will save
     * about 50% of the disk space, but will slow down read and write
     * operations slightly.
     * <p>
     *
     * <b>NOTE:</b> This setting only affects writes; it is not necessary to enable
     * compression when reading, even if compression was enabled when
     * writing.
     */
    public void compressed() {
        if (configured) {
            throw new InvalidOperationException("cannot change compression property after database" +
                " initialization");
        }
        this.compressed = true;
    }

    /**
     * Disables auto commit. If disabled, unsaved changes will not be written
     * into disk until {@link org.dizitart.no2.Nitrite#commit()} is called.
     * <p>
     * By default auto commit is enabled.
     */
    public void disableAutoCommit() {
        if (configured) {
            throw new InvalidOperationException("cannot change the auto commit property after database" +
                " initialization");
        }
        this.autoCommit = false;
    }

    /**
     * Disables auto compact before close. If disabled, compaction will not
     * be performed. Disabling would increase close performance.
     * <p>
     * By default auto compact is enabled.
     */
    public void disableAutoCompact() {
        if (configured) {
            throw new InvalidOperationException("cannot change auto compact property after database" +
                " initialization");
        }
        this.autoCompact = false;
    }

    @Override
    public void addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
    }

    void configured() {
        this.configured = true;
    }
}
