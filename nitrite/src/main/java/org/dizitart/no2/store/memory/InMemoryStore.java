package org.dizitart.no2.store.memory;

import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.AbstractNitriteStore;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.dizitart.no2.common.Constants.NITRITE_VERSION;

/**
 * The nitrite in-memory store.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public final class InMemoryStore extends AbstractNitriteStore<InMemoryConfig> {
    private final Map<String, NitriteMap<?, ?>> nitriteMapRegistry;
    private final Map<String, NitriteRTree<?, ?>> nitriteRTreeMapRegistry;
    private volatile boolean closed = false;

    /**
     * Instantiates a new {@link InMemoryStore}.
     */
    public InMemoryStore() {
        super();
        this.nitriteMapRegistry = new ConcurrentHashMap<>();
        this.nitriteRTreeMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void openOrCreate() {
        initEventBus();
        alert(StoreEvents.Opened);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void commit() {
        alert(StoreEvents.Commit);
    }

    @Override
    public void close() {
        closed = true;

        for (NitriteMap<?, ?> map : nitriteMapRegistry.values()) {
            map.close();
        }

        for (NitriteRTree<?, ?> rTree : nitriteRTreeMapRegistry.values()) {
            rTree.close();
        }

        nitriteMapRegistry.clear();
        nitriteRTreeMapRegistry.clear();
        alert(StoreEvents.Closed);
        eventBus.close();
    }

    @Override
    public boolean hasMap(String mapName) {
        return nitriteMapRegistry.containsKey(mapName) || nitriteRTreeMapRegistry.containsKey(mapName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key, Value> NitriteMap<Key, Value> openMap(String mapName, Class<?> keyType, Class<?> valueType) {
        if (nitriteMapRegistry.containsKey(mapName)) {
            NitriteMap<Key, Value> nitriteMap = (NitriteMap<Key, Value>) nitriteMapRegistry.get(mapName);
            if (nitriteMap.isClosed()) {
                nitriteMapRegistry.remove(mapName);
            } else {
                return nitriteMap;
            }
        }

        NitriteMap<Key, Value> nitriteMap = new InMemoryMap<>(mapName, this);
        nitriteMapRegistry.put(mapName, nitriteMap);

        return nitriteMap;
    }

    @Override
    public void closeMap(String mapName) {
        // nothing to close as it is volatile map, moreover,
        // removing it from registry means losing the map
    }

    @Override
    public void closeRTree(String rTreeName) {
        // nothing to close as it is volatile map, moreover,
        // removing it from registry means losing the map
    }

    @Override
    public void removeMap(String mapName) {
        if (nitriteMapRegistry.containsKey(mapName)) {
            NitriteMap<?, ?> nitriteMap = nitriteMapRegistry.get(mapName);
            if (!nitriteMap.isClosed() && !nitriteMap.isDropped()) {
                nitriteMap.clear();
                nitriteMap.close();
            }
            nitriteMapRegistry.remove(mapName);
            getCatalog().remove(mapName);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String rTreeName,
                                                                               Class<?> keyType,
                                                                               Class<?> valueType) {
        if (nitriteRTreeMapRegistry.containsKey(rTreeName)) {
            return (InMemoryRTree<Key, Value>) nitriteRTreeMapRegistry.get(rTreeName);
        }

        NitriteRTree<Key, Value> rTree = new InMemoryRTree<>();
        nitriteRTreeMapRegistry.put(rTreeName, rTree);

        return rTree;
    }

    @Override
    public String getStoreVersion() {
        return "InMemory/" + NITRITE_VERSION;
    }

    private void initEventBus() {
        if (getStoreConfig().eventListeners() != null) {
            for (StoreEventListener eventListener : getStoreConfig().eventListeners()) {
                subscribe(eventListener);
            }
        }
    }
}
