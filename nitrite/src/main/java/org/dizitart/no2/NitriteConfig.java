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
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.module.NitriteModule;
import org.dizitart.no2.module.NitritePlugin;
import org.dizitart.no2.module.PluginManager;
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
public class NitriteConfig {
    /**
     * Gets the embedded field separator character. Default value
     * is `.` unless set explicitly.
     *
     * @return the embedded field separator character.
     */
    @Getter
    private static String fieldSeparator = ".";

    @Getter(AccessLevel.PACKAGE)
    private final PluginManager pluginManager;

    @Getter
    private final Map<Integer, TreeMap<Integer, Migration>> migrations;

    @Getter
    private Integer schemaVersion = Constants.INITIAL_SCHEMA_VERSION;

    private boolean configured = false;

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
            throw new InvalidOperationException("cannot change the separator after database" +
                " initialization");
        }
        NitriteConfig.fieldSeparator = separator;
    }

    /**
     * Loads {@link NitritePlugin} instances.
     *
     * @param module the {@link NitriteModule} instances.
     * @return the {@link NitriteConfig} instance.
     */
    public NitriteConfig loadModule(NitriteModule module) {
        if (configured) {
            throw new InvalidOperationException("cannot load module after database" +
                " initialization");
        }
        pluginManager.loadModule(module);
        return this;
    }

    @SuppressWarnings("Java8MapApi")
    public NitriteConfig addMigration(Migration migration) {
        if (configured) {
            throw new InvalidOperationException("cannot add migration steps after database" +
                " initialization");
        }

        if (migration != null) {
            final int start = migration.getStartVersion();
            final int end = migration.getEndVersion();
            TreeMap<Integer, Migration> targetMap = migrations.get(start);
            if (targetMap == null) {
                targetMap = new TreeMap<>();
                migrations.put(start, targetMap);
            }
            Migration existing = targetMap.get(end);
            if (existing != null) {
                log.warn("Overriding migration " + existing + " with " + migration);
            }
            targetMap.put(end, migration);
        }
        return this;
    }

    public NitriteConfig schemaVersion(Integer version) {
        if (configured) {
            throw new InvalidOperationException("cannot add schema version info after database" +
                " initialization");
        }
        this.schemaVersion = version;
        return this;
    }

    /**
     * Auto configures nitrite database with default configuration values and
     * default built-in plugins.
     *
     */
    public void autoConfigure() {
        if (configured) {
            throw new InvalidOperationException("cannot execute autoconfigure after database" +
                " initialization");
        }
        pluginManager.findAndLoadPlugins();
    }

    /**
     * Finds an {@link Indexer} by indexType.
     *
     * @param indexType the type of {@link Indexer} to find.
     * @return the {@link Indexer}
     */
    public Indexer findIndexer(String indexType) {
        Indexer indexer = pluginManager.getIndexerMap().get(indexType);
        indexer.initialize(this);
        return indexer;
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

    void initialized() {
        this.configured = true;
        this.pluginManager.initializePlugins();
    }
}
