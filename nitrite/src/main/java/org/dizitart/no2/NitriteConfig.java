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
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.common.module.NitriteModule;
import org.dizitart.no2.common.module.NitritePlugin;
import org.dizitart.no2.common.module.PluginManager;
import org.dizitart.no2.store.NitriteStore;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class to configure {@link Nitrite} database.
 *
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
@Slf4j
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
    private static String fieldSeparator = ".";

    @Getter
    private final Map<Integer, TreeMap<Integer, Migration>> migrations;

    @Getter
    private Integer schemaVersion = Constants.INITIAL_SCHEMA_VERSION;

    /**
     * Instantiates a new {@link NitriteConfig}.
     */
    public NitriteConfig() {
        this.pluginManager = new PluginManager(this);
        this.migrations = new HashMap<>();
    }

    /**
     * Sets the embedded field separator character. Default value
     * is `.`
     *
     * @param separator the separator
     */
    public void fieldSeparator(String separator) {
        if (configured) {
            throw new InvalidOperationException("Cannot change the separator after database" +
                " initialization");
        }
        NitriteConfig.fieldSeparator = separator;
    }

    /**
     * Loads {@link NitritePlugin} instances defined in the {@link NitriteModule}.
     *
     * @param module the {@link NitriteModule} instances.
     * @return the {@link NitriteConfig} instance.
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
     * Adds schema migration instructions.
     *
     * @param migration the migration
     * @return the nitrite config
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
                log.warn("Overriding migration " + existing + " with " + migration);
            }
            targetMap.put(end, migration);
        }
        return this;
    }

    /**
     * Sets the current schema version.
     *
     * @param version the version
     * @return the nitrite config
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
     * Autoconfigures nitrite database with default configuration values and
     * default built-in plugins.
     */
    public void autoConfigure() {
        if (configured) {
            throw new InvalidOperationException("Cannot execute autoconfigure after database" +
                " initialization");
        }
        pluginManager.findAndLoadPlugins();
    }

    /**
     * Finds a {@link NitriteIndexer} by indexType.
     *
     * @param indexType the type of {@link NitriteIndexer} to find.
     * @return the {@link NitriteIndexer}
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
     * Gets the {@link NitriteMapper} instance.
     *
     * @return the {@link NitriteMapper}
     */
    public NitriteMapper nitriteMapper() {
        return pluginManager.getNitriteMapper();
    }

    /**
     * Gets {@link NitriteStore} instance.
     *
     * @return the {@link NitriteStore}
     */
    public NitriteStore<?> getNitriteStore() {
        return pluginManager.getNitriteStore();
    }

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
