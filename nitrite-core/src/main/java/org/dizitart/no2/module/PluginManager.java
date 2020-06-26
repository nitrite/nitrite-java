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
import org.dizitart.no2.exceptions.PluginException;
import org.dizitart.no2.index.*;
import org.dizitart.no2.mapper.MappableMapper;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteStore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
@Getter
public class PluginManager {
    private final Map<String, Indexer> indexerMap;
    private NitriteMapper nitriteMapper;
    private NitriteStore nitriteStore;
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
        if (!indexerMap.isEmpty()) {
            for (Indexer indexer : indexerMap.values()) {
                initializePlugin(indexer);
            }
        }

        if (nitriteMapper != null) {
            initializePlugin(nitriteMapper);
        }

        if (nitriteStore != null) {
            initializePlugin(nitriteStore);
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
            this.nitriteStore = (NitriteStore) plugin;
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
        if (plugin instanceof Indexer) {
            Indexer indexer = (Indexer) plugin;
            if (indexerMap.containsKey(indexer.getIndexType())) {
                throw new PluginException("multiple Indexer found for type "
                    + indexer.getIndexType());
            }
            this.indexerMap.put(indexer.getIndexType(), indexer);
        }
    }

    private void loadInternalPlugins() {
        if (!indexerMap.containsKey(IndexType.Unique)) {
            loadPlugin(new UniqueIndexer());
        }

        if (!indexerMap.containsKey(IndexType.NonUnique)) {
            loadPlugin(new NonUniqueIndexer());
        }

        if (!indexerMap.containsKey(IndexType.Fulltext)) {
            loadPlugin(new NitriteTextIndexer());
        }

        if (nitriteMapper == null) {
            loadPlugin(new MappableMapper());
        }
    }
}
