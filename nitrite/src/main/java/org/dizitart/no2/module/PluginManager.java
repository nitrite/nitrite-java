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

package org.dizitart.no2.module;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.PluginException;
import org.dizitart.no2.index.*;
import org.dizitart.no2.mapper.MappableMapper;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.memory.InMemoryStoreModule;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
@Getter
public class PluginManager {
    private final Map<String, NitriteIndexer> indexerMap;
    private NitriteMapper nitriteMapper;
    private NitriteStore<?> nitriteStore;
    private final NitriteConfig nitriteConfig;

    public PluginManager(NitriteConfig nitriteConfig) {
        this.indexerMap = new HashMap<>();
        this.nitriteConfig = nitriteConfig;
    }

    public void loadModule(NitriteModule module) {
        if (module != null && module.plugins() != null) {
            for (NitritePlugin plugin : module.plugins()) {
                loadPlugin(plugin);
            }
        }
    }

    public void findAndLoadPlugins() {
        try {
            loadInternalPlugins();
        } catch (Exception e) {
            log.error("Error while loading internal plugins", e);
            throw new PluginException("error while loading internal plugins", e);
        }
    }

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

    private void loadPlugin(NitritePlugin plugin) {
        populatePlugins(plugin);
    }

    private void initializePlugin(NitritePlugin plugin) {
        plugin.initialize(nitriteConfig);
    }

    private void populatePlugins(NitritePlugin plugin) {
        if (plugin != null) {
            loadIfIndexer(plugin);
            loadIfNitriteMapper(plugin);
            loadIfNitriteStore(plugin);
        }
    }

    private void loadIfNitriteStore(NitritePlugin plugin) {
        if (plugin instanceof NitriteStore) {
            if (nitriteStore != null) {
                throw new PluginException("multiple NitriteStore found");
            }
            this.nitriteStore = (NitriteStore<?>) plugin;
        }
    }

    private void loadIfNitriteMapper(NitritePlugin plugin) {
        if (plugin instanceof NitriteMapper) {
            if (nitriteMapper != null) {
                throw new PluginException("multiple NitriteMapper found");
            }
            this.nitriteMapper = (NitriteMapper) plugin;
        }
    }

    private synchronized void loadIfIndexer(NitritePlugin plugin) {
        if (plugin instanceof NitriteIndexer) {
            NitriteIndexer nitriteIndexer = (NitriteIndexer) plugin;
            if (indexerMap.containsKey(nitriteIndexer.getIndexType())) {
                throw new PluginException("multiple Indexer found for type "
                    + nitriteIndexer.getIndexType());
            }
            this.indexerMap.put(nitriteIndexer.getIndexType(), nitriteIndexer);
        }
    }

    private void loadInternalPlugins() {
        if (!indexerMap.containsKey(IndexType.Unique)) {
            log.debug("Loading default unique indexer");
            loadPlugin(new UniqueIndexer());
        }

        if (!indexerMap.containsKey(IndexType.NonUnique)) {
            log.debug("Loading default non-unique indexer");
            loadPlugin(new NonUniqueIndexer());
        }

        if (!indexerMap.containsKey(IndexType.Fulltext)) {
            log.debug("Loading nitrite text indexer");
            loadPlugin(new NitriteTextIndexer());
        }

        if (nitriteMapper == null) {
            log.debug("Loading mappable mapper");
            loadPlugin(new MappableMapper());
        }

        if (nitriteStore == null) {
            loadModule(new InMemoryStoreModule());
            log.warn("No persistent storage module found, creating an in-memory database");
        }
    }
}
