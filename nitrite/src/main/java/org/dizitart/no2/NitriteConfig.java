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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.module.NitriteModule;
import org.dizitart.no2.common.module.NitritePlugin;
import org.dizitart.no2.common.module.PluginManager;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.store.NitriteStore;

import java.util.*;

/**
 * NitriteConfig is a configuration class for Nitrite database.
 * 
 * @author Anindya Chatterjee.
 * @since 4.0
 */
@Slf4j(topic = "nitrite")
@ToString
public class NitriteConfig implements AutoCloseable {
    /**
     * Indicates if this {@link NitriteConfig} is already configured.
     */
    protected boolean configured = false;

    /**
     * Returns the {@link PluginManager} instance.
     */
    @Getter(AccessLevel.PACKAGE)
    protected final PluginManager pluginManager;

    @Getter
    /**
     * The separator used to separate field names in a nested field.
     */
    private static String fieldSeparator = ".";

    @Getter
    /**
     * A list of {@link EntityConverter} instances registered with the Nitrite database.
     */
    private final List<EntityConverter<?>> entityConverters;

    @Getter
    /**
     * A map of migrations to be applied to the database.
     */
    private final Map<Integer, TreeMap<Integer, Migration>> migrations;

    @Getter
    /**
     * The schema version of the Nitrite database. Defaults to
     * {@link Constants#INITIAL_SCHEMA_VERSION}.
     */
    private Integer schemaVersion = Constants.INITIAL_SCHEMA_VERSION;

    @Getter
    /**
     * Indicates if repository type validation is disabled.
     */
    private boolean repositoryTypeValidationDisabled = false;

    /**
     * Instantiates a new {@link NitriteConfig}.
     */
    public NitriteConfig() {
        this.migrations = new HashMap<>();
        this.entityConverters = new ArrayList<>();
        this.pluginManager = new PluginManager(this);
    }

    /**
     * Sets the field separator for Nitrite database.
     *
     * @param separator the field separator to be set.
     * @throws InvalidOperationException if the separator is attempted to be changed
     *                                   after database initialization.
     */
    public void fieldSeparator(String separator) {
        if (configured) {
            throw new InvalidOperationException("Cannot change the separator after database" +
                    " initialization");
        }
        NitriteConfig.fieldSeparator = separator;
    }

    /**
     * Disables repository type validation.
     *
     * @throws InvalidOperationException if the repository type validation is attempted to be
     *                                   changed after database initialization.
     */
    public void disableRepositoryTypeValidation() {
        if (configured) {
            throw new InvalidOperationException("Cannot change repository type validation after database" +
                " initialization");
        }
        this.repositoryTypeValidationDisabled = true;
    }

    /**
     * Registers an {@link EntityConverter} with the Nitrite database.
     *
     * @param entityConverter the {@link EntityConverter} to register
     * @throws InvalidOperationException if the converter is attempted to be registered
     *                                   after database initialization.
     */
    public void registerEntityConverter(EntityConverter<?> entityConverter) {
        if (configured) {
            throw new InvalidOperationException("Cannot register entity converter after database" +
                    " initialization");
        }
        entityConverters.add(entityConverter);
    }

    /**
     * Loads {@link NitritePlugin} instances defined in the {@link NitriteModule}
     * into the configuration.
     *
     * @param module the Nitrite module to be loaded
     * @return the Nitrite configuration instance
     * @throws InvalidOperationException if the database is already initialized
     */
    public NitriteConfig loadModule(NitriteModule module) {
        if (configured) {
            throw new InvalidOperationException("Cannot load module after database" +
                    " initialization");
        }
        pluginManager.loadModule(module);
        return this;
    }

    /**
     * Adds a migration step to the configuration. A migration step is a process of
     * updating the database from one version to another. If the database is already
     * initialized, then migration steps cannot be added.
     *
     * @param migration the migration step to be added.
     * @return the NitriteConfig instance.
     * @throws InvalidOperationException if migration steps are added after database
     *                                   initialization.
     */
    public NitriteConfig addMigration(Migration migration) {
        if (configured) {
            throw new InvalidOperationException("Cannot add migration steps after database" +
                    " initialization");
        }

        if (migration != null) {
            final int start = migration.getFromVersion();
            final int end = migration.getToVersion();
            TreeMap<Integer, Migration> targetMap = migrations.computeIfAbsent(start, k -> new TreeMap<>());
            Migration existing = targetMap.get(end);
            if (existing != null) {
                log.warn("Overriding migration {} with {}", existing, migration);
            }
            targetMap.put(end, migration);
        }
        return this;
    }

    /**
     * Sets the current schema version of the Nitrite database.
     *
     * @param version the current schema version.
     * @return the NitriteConfig instance.
     * @throws InvalidOperationException if the schema version is attempted to be
     *                                   added after database initialization.
     */
    public NitriteConfig currentSchemaVersion(Integer version) {
        if (configured) {
            throw new InvalidOperationException("Cannot add schema version info after database" +
                    " initialization");
        }
        this.schemaVersion = version;
        return this;
    }

    /**
     * Automatically configures Nitrite database by finding and loading plugins.
     * 
     * @throws InvalidOperationException if autoconfigure is executed after database
     *                                   initialization.
     */
    public void autoConfigure() {
        if (configured) {
            throw new InvalidOperationException("Cannot execute autoconfigure after database" +
                    " initialization");
        }

        pluginManager.findAndLoadPlugins();
    }

    /**
     * Finds the {@link NitriteIndexer} for the given index type.
     *
     * @param indexType the type of the index to find
     * @return the {@link NitriteIndexer} for the given index type
     * @throws IndexingException if no indexer is found for the given index type
     */
    public NitriteIndexer findIndexer(String indexType) {
        NitriteIndexer nitriteIndexer = pluginManager.getIndexerMap().get(indexType);
        if (nitriteIndexer != null) {
            nitriteIndexer.initialize(this);
            return nitriteIndexer;
        } else {
            throw new IndexingException("No indexer found for index type " + indexType);
        }
    }

    /**
     * Returns the {@link NitriteMapper} instance used by Nitrite.
     *
     * @return the NitriteMapper instance used by Nitrite.
     */
    public NitriteMapper nitriteMapper() {
        return pluginManager.getNitriteMapper();
    }

    /**
     * Returns the {@link NitriteStore} associated with this instance.
     *
     * @return the {@link NitriteStore} associated with this instance.
     */
    public NitriteStore<?> getNitriteStore() {
        return pluginManager.getNitriteStore();
    }

    /**
     * Closes the NitriteConfig instance and releases any resources 
     * associated with it.
     */
    @Override
    public void close() {
        if (pluginManager != null) {
            pluginManager.close();
        }
    }

    /**
     * Initializes this {@link NitriteConfig} instance.
     */
    protected void initialize() {
        this.configured = true;
        this.pluginManager.initializePlugins();
    }
}
