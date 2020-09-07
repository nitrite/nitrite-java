package org.dizitart.no2.store.tx;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.module.NitriteModule;
import org.dizitart.no2.store.NitriteStore;

/**
 * @author Anindya Chatterjee
 */
public class TransactionConfig extends NitriteConfig {
    private final NitriteConfig config;
    private final TransactionalStore<?> transactionalStore;

    public TransactionConfig(NitriteConfig config, TransactionalStore<?> transactionalStore) {
        this.config = config;
        this.transactionalStore = transactionalStore;
    }

    @Override
    public Indexer findIndexer(String indexType) {
        Indexer indexer = config.findIndexer(indexType);
        if (indexer != null) {
            indexer.initialize(this);
        }
        return indexer;
    }

    @Override
    public void fieldSeparator(String separator) {
        config.fieldSeparator(separator);
    }

    @Override
    public NitriteConfig loadModule(NitriteModule module) {
        return config.loadModule(module);
    }

    @Override
    public void autoConfigure() {
        config.autoConfigure();
    }

    @Override
    public NitriteMapper nitriteMapper() {
        return config.nitriteMapper();
    }

    @Override
    public NitriteStore<?> getNitriteStore() {
        return transactionalStore;
    }
}
