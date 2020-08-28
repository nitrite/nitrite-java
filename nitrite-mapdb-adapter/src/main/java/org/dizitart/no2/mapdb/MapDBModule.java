package org.dizitart.no2.mapdb;

import lombok.AccessLevel;
import lombok.Setter;
import org.dizitart.no2.module.NitritePlugin;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreModule;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class MapDBModule implements StoreModule {

    @Setter(AccessLevel.PACKAGE)
    private MapDBConfig storeConfig;

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
