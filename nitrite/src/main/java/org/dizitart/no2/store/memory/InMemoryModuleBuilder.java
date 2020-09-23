package org.dizitart.no2.store.memory;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Getter
@Accessors(fluent = true)
public class InMemoryModuleBuilder {
    private final Set<StoreEventListener> eventListeners;
    private final InMemoryConfig dbConfig;

    InMemoryModuleBuilder() {
        dbConfig = new InMemoryConfig();
        eventListeners = new HashSet<>();
    }

    public InMemoryModuleBuilder addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
        return this;
    }

    public InMemoryStoreModule build() {
        InMemoryStoreModule module = new InMemoryStoreModule();
        dbConfig.eventListeners(eventListeners());
        module.setStoreConfig(dbConfig);
        return module;
    }
}
