package org.dizitart.no2.mapdb;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Getter
@Setter
@Accessors(fluent = true)
public class MapDBModuleBuilder {
    private String filePath;
    private MapDBConfig dbConfig;

    @Setter(AccessLevel.NONE)
    private final Set<StoreEventListener> eventListeners;

    MapDBModuleBuilder() {
        dbConfig = new MapDBConfig();
        eventListeners = new HashSet<>();
    }

    public MapDBModuleBuilder addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
        return this;
    }

    public MapDBModule build() {
        MapDBModule module = new MapDBModule();

        module.setStoreConfig(dbConfig);
        return module;
    }
}
