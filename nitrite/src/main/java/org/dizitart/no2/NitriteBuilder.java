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

package org.dizitart.no2;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.module.NitriteModule;
import org.dizitart.no2.store.MVStoreConfig;
import org.dizitart.no2.store.events.StoreEventListener;
import org.h2.mvstore.OffHeapStore;

import java.io.File;

/**
 * A builder utility to create a {@link Nitrite} database instance.
 *
 * @author Anindya Chatterjee
 * @see Nitrite
 * @since 1.0
 */
@Slf4j
public abstract class NitriteBuilder {
    @Getter
    private NitriteConfig nitriteConfig;
    private MVStoreConfig storeConfig;

    private NitriteBuilder() {
    }

    /**
     * Creates a new {@link NitriteBuilder} instance.
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public static NitriteBuilder get() {
        NitriteBuilder builder = new NitriteBuilder() {
        };
        builder.nitriteConfig = NitriteConfig.create();
        builder.nitriteConfig.loadModule(new MVStoreModule());

        builder.storeConfig = MVStoreConfig.create();
        return builder;
    }

    /**
     * Sets file name for the file based store. If `file` is `null`
     * the builder will create an in-memory database.
     *
     * @param path the name of the file store.
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder filePath(String path) {
        this.storeConfig.filePath(path);
        return this;
    }

    /**
     * Sets file name for the file based store. If `file` is `null`
     * the builder will create an in-memory database.
     *
     * @param file the name of the file store.
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder filePath(File file) {
        if (file == null) {
            this.storeConfig.filePath((String) null);
        } else {
            this.storeConfig.filePath(file.getPath());
        }
        return this;
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
     * <b>NOTE:</b> If auto commit is disabled by {@link NitriteBuilder#disableAutoCommit()},
     * then buffer size has not effect.
     *
     * @param size the buffer size in KB
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder autoCommitBufferSize(int size) {
        this.storeConfig.autoCommitBufferSize(size);
        return this;
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
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder readOnly() {
        this.storeConfig.readOnly();
        return this;
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
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder compressed() {
        this.storeConfig.compressed();
        return this;
    }

    /**
     * Disables auto commit. If disabled, unsaved changes will not be written
     * into disk until {@link Nitrite#commit()} is called.
     * <p>
     * By default auto commit is enabled.
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder disableAutoCommit() {
        this.storeConfig.disableAutoCommit();
        return this;
    }

    /**
     * Disables auto compact before close. If disabled, compaction will not
     * be performed. Disabling would increase close performance.
     * <p>
     * By default auto compact is enabled.
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder disableAutoCompact() {
        this.storeConfig.disableAutoCompact();
        return this;
    }

    /**
     * Sets the embedded field separator character. Default value
     * is `.`
     *
     * @param separator the separator
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder fieldSeparator(String separator) {
        this.nitriteConfig.fieldSeparator(separator);
        return this;
    }

    /**
     * Loads {@link NitriteModule} instance.
     *
     * @param module the {@link NitriteModule} instance.
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder loadModule(NitriteModule module) {
        this.nitriteConfig.loadModule(module);
        return this;
    }

    /**
     * Enables off-heap storage for in-memory database.
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder enableOffHeapStorage() {
        this.storeConfig.fileStore(new OffHeapStore());
        return this;
    }

    /**
     * Adds a {@link StoreEventListener} instance and subscribe it to store event.
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder addStoreEventListener(StoreEventListener listener) {
        this.storeConfig.addStoreEventListener(listener);
        return this;
    }

    /**
     * Opens or creates a new nitrite database backed by mvstore. If it is an in-memory store,
     * then it will create a new one. If it is a file based store, and if the file does not
     * exists, then it will create a new file store and open; otherwise it will
     * open the existing file store.
     * <p>
     *
     *
     * <b>NOTE:</b> If the database is corrupted somehow then at the time of opening, it will
     * try to repair it using the last known good version. If still it fails to
     * recover, then it will throw a {@link org.dizitart.no2.exceptions.NitriteIOException}.
     *
     * @return the nitrite database instance.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if unable to create a new in-memory database.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if the database is corrupt and recovery fails.
     * @throws IllegalArgumentException                       if the directory does not exist.
     */
    public Nitrite openOrCreate() {
        this.nitriteConfig.storeConfig(storeConfig);
        this.nitriteConfig.autoConfigure();
        return Nitrite.openOrCreate(nitriteConfig);
    }

    /**
     * Opens or creates a new nitrite database backed by mvstore. If it is an in-memory store,
     * then it will create a new one. If it is a file based store, and if the file does not
     * exists, then it will create a new file store and open; otherwise it will
     * open the existing file store.
     * <p>
     * While creating a new database, it will use the specified user credentials.
     * While opening an existing database, it will use the specified credentials
     * to open it.
     * </p>
     *
     * <b>NOTE:</b> If the database is corrupted somehow then at the time of opening, it will
     * try to repair it using the last known good version. If still it fails to
     * recover, then it will throw a {@link org.dizitart.no2.exceptions.NitriteIOException}.
     *
     * @param username the username
     * @param password the password
     * @return the nitrite database instance.
     * @throws SecurityException                              if the user credentials are wrong or one of them is empty string.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if unable to create a new in-memory database.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if the database is corrupt and recovery fails.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if the directory does not exist.
     */
    public Nitrite openOrCreate(String username, String password) {
        this.nitriteConfig.storeConfig(storeConfig);
        this.nitriteConfig.autoConfigure();
        return Nitrite.openOrCreate(nitriteConfig, username, password);
    }
}
