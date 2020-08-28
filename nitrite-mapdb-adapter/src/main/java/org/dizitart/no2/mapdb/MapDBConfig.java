package org.dizitart.no2.mapdb;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Accessors(fluent = true)
public class MapDBConfig implements StoreConfig {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Set<StoreEventListener> eventListeners;

    @Getter @Setter(AccessLevel.PACKAGE)
    private String filePath;

    MapDBConfig() {
        eventListeners = new HashSet<>();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
    }
}
