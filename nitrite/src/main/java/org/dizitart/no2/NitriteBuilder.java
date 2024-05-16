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
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.exceptions.NitriteSecurityException;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.common.module.NitriteModule;

/**
 * The NitriteBuilder class provides a fluent API to configure and create a
 * Nitrite database instance.
 * 
 * @author Anindya Chatterjee
 * @see Nitrite
 * @since 1.0
 */
@Getter
public class NitriteBuilder {
    /**
     * The Nitrite configuration object.
     */
    private final NitriteConfig nitriteConfig;

    /**
     * Instantiates a new {@link NitriteBuilder}.
     */
    NitriteBuilder() {
        this.nitriteConfig = new NitriteConfig();
    }

    /**
     * Sets the field separator character for Nitrite. It is used to separate field
     * names in a nested document. For example, if a document has a field
     * <b>address</b> which is a nested document, then the field <b>street</b>
     * of the nested document can be accessed using <b>address.street</b> syntax.
     * <p>
     * The default value is [<b>.</b>].
     *
     * @param separator the field separator character to use
     * @return the NitriteBuilder instance
     */
    public NitriteBuilder fieldSeparator(String separator) {
        this.nitriteConfig.fieldSeparator(separator);
        return this;
    }

    /**
     * Disables the repository type validation for the Nitrite database.
     * <p>
     * Repository type validation is a feature in Nitrite that ensures the type of the objects
     * stored in the repository can be converted to and from {@link org.dizitart.no2.collection.Document}.
     * <p>
     * By default, the repository type validation is enabled. If you disable it, and if you try to
     * store an object that cannot be converted to a {@link org.dizitart.no2.collection.Document},
     * then Nitrite will throw an exception during the operation.
     *
     * @return the NitriteBuilder instance with repository type validation disabled
     * @see org.dizitart.no2.collection.Document
     * @see org.dizitart.no2.repository.ObjectRepository
     * @see org.dizitart.no2.common.mapper.EntityConverter
     * @since 4.3.0
     */
    public NitriteBuilder disableRepositoryTypeValidation() {
        this.nitriteConfig.disableRepositoryTypeValidation();
        return this;
    }

    /**
     * Registers an {@link EntityConverter} with the Nitrite database.
     * An {@link EntityConverter} is used to convert between an entity and a
     * {@link org.dizitart.no2.collection.Document}.
     * This method allows you to provide a custom converter for a specific class.
     *
     * @param entityConverter the {@link EntityConverter} to register
     * @return the NitriteBuilder instance
     */
    public NitriteBuilder registerEntityConverter(EntityConverter<?> entityConverter) {
        this.nitriteConfig.registerEntityConverter(entityConverter);
        return this;
    }

    /**
     * Loads a Nitrite module into the Nitrite database. The module can be used to
     * extend the functionality of Nitrite.
     *
     * @param module the {@link NitriteModule} to be loaded
     * @return the {@link NitriteBuilder} instance
     */
    public NitriteBuilder loadModule(NitriteModule module) {
        this.nitriteConfig.loadModule(module);
        return this;
    }

    /**
     * Adds one or more migrations to the Nitrite database. Migrations are used to
     * upgrade the database schema when the application version changes.
     *
     * @param migrations one or more migrations to add to the Nitrite database.
     * @return the NitriteBuilder instance.
     */
    public NitriteBuilder addMigrations(Migration... migrations) {
        for (Migration migration : migrations) {
            this.nitriteConfig.addMigration(migration);
        }
        return this;
    }

    /**
     * Sets the schema version for the Nitrite database.
     *
     * @param version the schema version to set
     * @return the NitriteBuilder instance
     */
    public NitriteBuilder schemaVersion(Integer version) {
        this.nitriteConfig.currentSchemaVersion(version);
        return this;
    }

    /**
     * Opens or creates a new Nitrite database. If it is configured as in-memory
     * database, then it will create a new database everytime. If it is configured
     * as a file based database, and if the file does not exist, then it will create
     * a new file store and open the database; otherwise it will open the existing
     * database file.
     *
     * @return the nitrite database instance.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if unable to create a
     *                                                        new in-memory
     *                                                        database.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if the database is
     *                                                        corrupt and recovery
     *                                                        fails.
     * @throws IllegalArgumentException                       if the directory does
     *                                                        not exist.
     */
    public Nitrite openOrCreate() {
        this.nitriteConfig.autoConfigure();
        Runtime.getRuntime().addShutdownHook(new Thread(ThreadPoolManager::shutdownAllThreadPools));
        NitriteDatabase db = new NitriteDatabase(nitriteConfig);
        db.initialize(null, null);
        return db;
    }

    /**
     * Opens or creates a new Nitrite database with the given username and password.
     * If it is configured as in-memory database, then it will create a new database
     * everytime. If it is configured as a file based database, and if the file
     * does not exist, then it will create a new file store and open the database;
     * otherwise it will open the existing database file.
     * 
     * <p>
     * NOTE: Both username and password must be provided or both must be null.
     *
     * @param username the username
     * @param password the password
     * @return the nitrite database instance.
     * @throws NitriteSecurityException                       if the user
     *                                                        credentials are wrong
     *                                                        or one of them is
     *                                                        empty string.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if unable to create a
     *                                                        new in-memory
     *                                                        database.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if the database is
     *                                                        corrupt and recovery
     *                                                        fails.
     * @throws org.dizitart.no2.exceptions.NitriteIOException if the directory does
     *                                                        not exist.
     */
    public Nitrite openOrCreate(String username, String password) {
        this.nitriteConfig.autoConfigure();
        Runtime.getRuntime().addShutdownHook(new Thread(ThreadPoolManager::shutdownAllThreadPools));
        NitriteDatabase db = new NitriteDatabase(nitriteConfig);
        db.initialize(username, password);
        return db;
    }
}
