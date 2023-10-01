package org.dizitart.no2.rocksdb;

import lombok.AccessLevel;
import lombok.Setter;
import org.dizitart.no2.common.module.NitritePlugin;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreModule;

import java.util.Set;

import static org.dizitart.no2.common.util.Iterables.setOf;

/**
 * A Nitrite store module that provides a RocksDB implementation of the
 * NitriteStore interface.
 * 
 * @since 4.0
 * @see NitriteStore
 * @see StoreModule
 * @author Anindya Chatterjee
 */
public class RocksDBModule implements StoreModule {
    @Setter(AccessLevel.PACKAGE)
    /**
     * The RocksDB configuration for the Nitrite database store.
     */
    private RocksDBConfig storeConfig;

    /**
     * Instantiates a new RocksDB module.
     */
    public RocksDBModule(String path) {
        this.storeConfig = new RocksDBConfig();
        this.storeConfig.filePath(path);
    }

    /**
     * Returns a set of Nitrite plugins.
     *
     * @return a set of Nitrite plugins.
     */
    @Override
    public Set<NitritePlugin> plugins() {
        return setOf(getStore());
    }

    /**
     * Returns a new instance of {@link RocksDBModuleBuilder} to build a
     * {@link RocksDBModule} with custom configuration.
     *
     * @return a new instance of {@link RocksDBModuleBuilder}.
     */
    public static RocksDBModuleBuilder withConfig() {
        return new RocksDBModuleBuilder();
    }

    /**
     * Returns a new instance of {@link NitriteStore} backed by RocksDB.
     *
     * @return a new instance of {@link NitriteStore} backed by RocksDB.
     */
    public NitriteStore<?> getStore() {
        RocksDBStore store = new RocksDBStore();
        store.setStoreConfig(storeConfig);
        return store;
    }
}
