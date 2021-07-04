package org.dizitart.no2.transaction;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.module.NitriteModule;
import org.dizitart.no2.store.NitriteStore;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Slf4j
class TransactionConfig extends NitriteConfig {
    private final NitriteConfig config;

    public TransactionConfig(NitriteConfig config) {
        super();
        this.config = config;
    }

    @Override
    public NitriteIndexer findIndexer(String indexType) {
        NitriteIndexer nitriteIndexer = pluginManager.getIndexerMap().get(indexType);
        if (nitriteIndexer != null) {
            nitriteIndexer.initialize(this);
            return nitriteIndexer;
        } else {
            throw new IndexingException("no indexer found for index type " + indexType);
        }
    }

    @Override
    public void fieldSeparator(String separator) {
        config.fieldSeparator(separator);
    }

    @Override
    public NitriteConfig loadModule(NitriteModule module) {
        pluginManager.loadModule(module);
        return this;
    }

    @Override
    public void autoConfigure() {
        pluginManager.findAndLoadPlugins();
    }

    @Override
    public NitriteMapper nitriteMapper() {
        return config.nitriteMapper();
    }

    @Override
    public NitriteStore<?> getNitriteStore() {
        return pluginManager.getNitriteStore();
    }

    @Override
    public void initialize() {
        super.initialize();
    }
}
