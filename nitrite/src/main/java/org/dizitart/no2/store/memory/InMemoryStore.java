package org.dizitart.no2.store.memory;

import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.AbstractNitriteStore;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

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
        Consumer<Map.Entry<?, ?>> closeConsumer = entry -> {
            if (entry.getValue() instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) entry.getValue()).close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        nitriteMapRegistry.entrySet().forEach(closeConsumer);
        nitriteRTreeMapRegistry.entrySet().forEach(closeConsumer);

        nitriteMapRegistry.clear();
        nitriteRTreeMapRegistry.clear();
        super.close();
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
    @SuppressWarnings("unchecked")
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String rTreeName,
                                                                               Class<?> keyType,
                                                                               Class<?> valueType) {
        if (nitriteRTreeMapRegistry.containsKey(rTreeName)) {
            return (InMemoryRTree<Key, Value>) nitriteRTreeMapRegistry.get(rTreeName);
        }

        NitriteRTree<Key, Value> rTree = new InMemoryRTree<>(rTreeName, this);
        nitriteRTreeMapRegistry.put(rTreeName, rTree);

        return rTree;
    }

    @Override
    public void closeMap(String mapName) {
        nitriteMapRegistry.remove(mapName);
    }

    @Override
    public void closeRTree(String rTreeName) {
        nitriteRTreeMapRegistry.remove(rTreeName);
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
    public void removeRTree(String rTreeName) {
        if (nitriteRTreeMapRegistry.containsKey(rTreeName)) {
            NitriteRTree<?, ?> rTree = nitriteRTreeMapRegistry.get(rTreeName);
            rTree.close();
            nitriteRTreeMapRegistry.remove(rTreeName);
            getCatalog().remove(rTreeName);
        }
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
