package org.dizitart.no2.store.memory;

import lombok.AccessLevel;
import lombok.Setter;
import org.dizitart.no2.common.module.NitritePlugin;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreModule;

import java.util.Set;

import static org.dizitart.no2.common.util.Iterables.setOf;

/**
 * The in-memory store module for nitrite.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class InMemoryStoreModule implements StoreModule {

    @Setter(AccessLevel.PACKAGE)
    private InMemoryConfig storeConfig;

    /**
     * Instantiates a new {@link InMemoryStoreModule}.
     */
    public InMemoryStoreModule() {
        this.storeConfig = new InMemoryConfig();
    }

    /**
     * Creates an {@link InMemoryModuleBuilder} to configure the in-memory store.
     *
     * @return the in memory module builder
     */
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
