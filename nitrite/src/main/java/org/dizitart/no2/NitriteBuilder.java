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

package org.dizitart.no2;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.exceptions.NitriteSecurityException;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.common.module.NitriteModule;

/**
 * A builder utility to create a {@link Nitrite} database instance.
 *
 * @author Anindya Chatterjee
 * @see Nitrite
 * @since 1.0
 */
@Slf4j
public class NitriteBuilder {
    @Getter
    private final NitriteConfig nitriteConfig;

    /**
     * Instantiates a new {@link NitriteBuilder}.
     */
    NitriteBuilder() {
        this.nitriteConfig = new NitriteConfig();
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
     * Adds instructions to perform during schema migration.
     *
     * @param migrations the migrations
     * @return the nitrite builder
     */
    public NitriteBuilder addMigrations(Migration... migrations) {
        for (Migration migration : migrations) {
            this.nitriteConfig.addMigration(migration);
        }
        return this;
    }

    /**
     * Sets the current schema version.
     *
     * @param version the version
     * @return the nitrite builder
     */
    public NitriteBuilder schemaVersion(Integer version) {
        this.nitriteConfig.schemaVersion(version);
        return this;
    }

    /**
     * Opens or creates a new nitrite database backed by mvstore. If it is an in-memory store,
     * then it will create a new one. If it is a file based store, and if the file does not
     * exists, then it will create a new file store and open; otherwise it will
     * open the existing file store.
     * <p>
     * <p>
     * NOTE: If the database is corrupted somehow then at the time of opening, it will
     * try to repair it using the last known good version. If still it fails to
     * recover, then it will throw a {@link org.dizitart.no2.exceptions.NitriteIOException}.
     *
     * @return the nitrite database instance.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if unable to create a new in-memory database.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if the database is corrupt and recovery fails.
     * @throws IllegalArgumentException                       if the directory does not exist.
     */
    public Nitrite openOrCreate() {
        this.nitriteConfig.autoConfigure();
        return new NitriteDatabase(nitriteConfig);
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
     * <p>
     * NOTE: If the database is corrupted somehow then at the time of opening, it will
     * try to repair it using the last known good version. If still it fails to
     * recover, then it will throw a {@link org.dizitart.no2.exceptions.NitriteIOException}.
     *
     * @param username the username
     * @param password the password
     * @return the nitrite database instance.
     * @throws NitriteSecurityException                              if the user credentials are wrong or one of them is empty string.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if unable to create a new in-memory database.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if the database is corrupt and recovery fails.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if the directory does not exist.
     */
    public Nitrite openOrCreate(String username, String password) {
        this.nitriteConfig.autoConfigure();
        Runtime.getRuntime().addShutdownHook(new Thread(ThreadPoolManager::shutdownThreadPools));
        return new NitriteDatabase(username, password, nitriteConfig);
    }
}
