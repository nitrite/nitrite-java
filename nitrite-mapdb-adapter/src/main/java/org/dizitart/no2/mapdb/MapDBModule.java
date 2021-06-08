package org.dizitart.no2.mapdb;

import lombok.AccessLevel;
import lombok.Setter;
import org.dizitart.no2.common.module.NitritePlugin;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreModule;

import java.util.Set;

import static org.dizitart.no2.common.util.Iterables.setOf;

/**
 * @author Anindya Chatterjee
 */
public class MapDBModule implements StoreModule {

    @Setter(AccessLevel.PACKAGE)
    private MapDBConfig storeConfig;

    public MapDBModule(String path) {
        this.storeConfig = new MapDBConfig();
        this.storeConfig.filePath(path);
    }

    public static MapDBModuleBuilder withConfig() {
        return new MapDBModuleBuilder();
    }

    @Override
    public NitriteStore<?> getStore() {
        MapDBStore store = new MapDBStore();
        store.setStoreConfig(storeConfig);
        return store;
    }

    @Override
    public Set<NitritePlugin> plugins() {
        return setOf(getStore());
    }
}
