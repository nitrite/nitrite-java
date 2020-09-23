package org.dizitart.no2.store.memory;

import lombok.AccessLevel;
import lombok.Setter;
import org.dizitart.no2.module.NitritePlugin;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreModule;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class InMemoryStoreModule implements StoreModule {

    @Setter(AccessLevel.PACKAGE)
    private InMemoryConfig storeConfig;

    public InMemoryStoreModule() {
        this.storeConfig = new InMemoryConfig();
    }

    public static InMemoryModuleBuilder withConfig() {
        return new InMemoryModuleBuilder();
    }

    @Override
    public NitriteStore<?> getStore() {
        InMemoryStore store = new InMemoryStore();
        store.setStoreConfig(storeConfig);
        return store;
    }

    @Override
    public Set<NitritePlugin> plugins() {
        return setOf(getStore());
    }
}
