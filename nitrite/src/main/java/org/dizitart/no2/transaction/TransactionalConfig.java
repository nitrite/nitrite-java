package org.dizitart.no2.transaction;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.module.NitriteModule;
import org.dizitart.no2.store.NitriteStore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
class TransactionalConfig extends NitriteConfig {
    private final NitriteConfig config;
    private final TransactionalStore<?> transactionalStore;
    private final Map<String, Indexer> indexerMap;

    public TransactionalConfig(NitriteConfig config, TransactionalStore<?> transactionalStore) {
        this.config = config;
        this.transactionalStore = transactionalStore;
        this.indexerMap = new HashMap<>();
    }

    @Override
    public Indexer findIndexer(String indexType) {
        if (indexerMap.containsKey(indexType)) {
            return indexerMap.get(indexType);
        }

        try {
            Indexer indexer = config.findIndexer(indexType).clone();
            if (indexer != null) {
                indexer.initialize(this);
                indexerMap.put(indexType, indexer);
            }
            return indexer;
        } catch (CloneNotSupportedException e) {
            log.error("Failed to clone indexer", e);
            throw new NitriteIOException("error while cloning indexer", e);
        }
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
