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

package org.dizitart.no2.common.module;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.PluginException;
import org.dizitart.no2.index.*;
import org.dizitart.no2.common.mapper.MappableMapper;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.memory.InMemoryStoreModule;

import java.util.HashMap;
import java.util.Map;

/**
 * The nitrite database plugin manager. It loads the nitrite plugins
 * before opening the database.
 *
 * @see NitriteModule
 * @see NitritePlugin
 * @author Anindya Chatterjee.
 * @since 4.0
 */
@Slf4j
@Getter
public class PluginManager implements AutoCloseable {
    private final Map<String, NitriteIndexer> indexerMap;
    private final NitriteConfig nitriteConfig;
    private NitriteMapper nitriteMapper;
    private NitriteStore<?> nitriteStore;

    /**
     * Instantiates a new {@link PluginManager}.
     *
     * @param nitriteConfig the nitrite config
     */
    public PluginManager(NitriteConfig nitriteConfig) {
        this.indexerMap = new HashMap<>();
        this.nitriteConfig = nitriteConfig;
    }

    /**
     * Loads a {@link NitriteModule} instance.
     *
     * @param module the module
     */
    public void loadModule(NitriteModule module) {
        if (module != null && module.plugins() != null) {
            for (NitritePlugin plugin : module.plugins()) {
                loadPlugin(plugin);
            }
        }
    }

    /**
     * Find and loads all nitrite plugins configured.
     */
    public void findAndLoadPlugins() {
        try {
            loadInternalPlugins();
        } catch (Exception e) {
            log.error("Error while loading internal plugins", e);
            throw new PluginException("error while loading internal plugins", e);
        }
    }

    /**
     * Initializes all plugins instances.
     */
    public void initializePlugins() {
        if (nitriteStore != null) {
            initializePlugin(nitriteStore);
        } else {
            log.error("No storage engine found. Please ensure that a storage module has been loaded properly");
            throw new NitriteIOException("no storage engine found");
        }

        if (nitriteMapper != null) {
            initializePlugin(nitriteMapper);
        }

        if (!indexerMap.isEmpty()) {
            for (NitriteIndexer nitriteIndexer : indexerMap.values()) {
                initializePlugin(nitriteIndexer);
            }
        }
    }

    @Override
    public void close() {
        for (NitriteIndexer nitriteIndexer : indexerMap.values()) {
            nitriteIndexer.close();
        }

        if (nitriteMapper != null) {
            nitriteMapper.close();
        }

        if (nitriteStore != null) {
            nitriteStore.close();
        }
    }

    private void loadPlugin(NitritePlugin plugin) {
        populatePlugins(plugin);
    }

    private void initializePlugin(NitritePlugin plugin) {
        plugin.initialize(nitriteConfig);
    }

    private void populatePlugins(NitritePlugin plugin) {
        if (plugin != null) {
            if (plugin instanceof NitriteIndexer) {
                loadIndexer((NitriteIndexer) plugin);
            } else if (plugin instanceof NitriteMapper) {
                loadNitriteMapper((NitriteMapper) plugin);
            } else if (plugin instanceof NitriteStore) {
                loadNitriteStore((NitriteStore<?>) plugin);
            } else {
                plugin.close();
                throw new PluginException("invalid plugin loaded " + plugin);
            }
        }
    }

    private void loadNitriteStore(NitriteStore<?> nitriteStore) {
        if (this.nitriteStore != null) {
            nitriteStore.close();
            throw new PluginException("multiple NitriteStore found");
        }
        this.nitriteStore = nitriteStore;
    }

    private void loadNitriteMapper(NitriteMapper nitriteMapper) {
        if (this.nitriteMapper != null) {
            nitriteMapper.close();
            throw new PluginException("multiple NitriteMapper found");
        }
        this.nitriteMapper = nitriteMapper;
    }

    private synchronized void loadIndexer(NitriteIndexer nitriteIndexer) {
        if (indexerMap.containsKey(nitriteIndexer.getIndexType())) {
            nitriteIndexer.close();
            throw new PluginException("multiple Indexer found for type "
                + nitriteIndexer.getIndexType());
        }
        this.indexerMap.put(nitriteIndexer.getIndexType(), nitriteIndexer);
    }

    protected void loadInternalPlugins() {
        if (!indexerMap.containsKey(IndexType.UNIQUE)) {
            log.debug("Loading default unique indexer");
            NitritePlugin plugin = new UniqueIndexer();
            loadPlugin(plugin);
        }

        if (!indexerMap.containsKey(IndexType.NON_UNIQUE)) {
            log.debug("Loading default non-unique indexer");
            NitritePlugin plugin = new NonUniqueIndexer();
            loadPlugin(plugin);
        }

        if (!indexerMap.containsKey(IndexType.FULL_TEXT)) {
            log.debug("Loading nitrite text indexer");
            NitritePlugin plugin = new NitriteTextIndexer();
            loadPlugin(plugin);
        }

        if (nitriteMapper == null) {
            log.debug("Loading mappable mapper");
            NitritePlugin plugin = new MappableMapper();
            loadPlugin(plugin);
        }

        if (nitriteStore == null) {
            loadModule(new InMemoryStoreModule());
            log.warn("No persistent storage module found, creating an in-memory database");
        }
    }
}
