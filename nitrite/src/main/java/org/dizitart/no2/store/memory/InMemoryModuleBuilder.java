package org.dizitart.no2.store.memory;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.HashSet;
import java.util.Set;

/**
 * The in-memory store module builder.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Getter
@Accessors(fluent = true)
public class InMemoryModuleBuilder {
    private final Set<StoreEventListener> eventListeners;
    private final InMemoryConfig dbConfig;

    /**
     * Instantiates a new {@link InMemoryModuleBuilder}.
     */
    InMemoryModuleBuilder() {
        dbConfig = new InMemoryConfig();
        eventListeners = new HashSet<>();
    }

    /**
     * Adds a {@link StoreEventListener} to the in-memory module builder.
     *
     * @param listener the listener
     * @return the in memory module builder
     */
    public InMemoryModuleBuilder addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
        return this;
    }

    /**
     * Builds an in-memory store module.
     *
     * @return the in memory store module
     */
    public InMemoryStoreModule build() {
        InMemoryStoreModule module = new InMemoryStoreModule();
        dbConfig.eventListeners(eventListeners());
        module.setStoreConfig(dbConfig);
        return module;
    }
}
