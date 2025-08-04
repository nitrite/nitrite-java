package org.dizitart.no2.transaction;

import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.*;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
class TransactionStore<T extends StoreConfig> extends AbstractNitriteStore<T> {
    private final NitriteStore<T> primaryStore;
    private final Map<String, NitriteMap<?, ?>> mapRegistry;
    private final Map<String, NitriteRTree<?, ?>> rTreeRegistry;
    private final List<String> deletedMap = new ArrayList<>();

    public TransactionStore(NitriteStore<T> store) {
        this.primaryStore = store;
        this.mapRegistry = new ConcurrentHashMap<>();
        this.rTreeRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void openOrCreate() {
        // nothing to do
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void commit() {
        throw new InvalidOperationException("Call commit on transaction");
    }

    @Override
    public void close() {
        for (NitriteMap<?, ?> nitriteMap : mapRegistry.values()) {
            nitriteMap.close();
        }

        for (NitriteRTree<?, ?> rTree : rTreeRegistry.values()) {
            rTree.close();
        }

        mapRegistry.clear();
        rTreeRegistry.clear();
        eventBus.close();
    }

    @Override
    public boolean hasMap(String mapName) {
        if (deletedMap.contains(mapName)) {
            return false;
        }

        boolean result = primaryStore.hasMap(mapName);
        if (!result) {
            result = mapRegistry.containsKey(mapName);
            if (!result) {
                return rTreeRegistry.containsKey(mapName);
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key, Value> NitriteMap<Key, Value> openMap(String mapName, Class<?> keyType, Class<?> valueType) {
        if (mapRegistry.containsKey(mapName)) {
            NitriteMap<Key, Value> nitriteMap = (NitriteMap<Key, Value>) mapRegistry.get(mapName);
            if (nitriteMap.isClosed()) {
                mapRegistry.remove(mapName);
            } else {
                return nitriteMap;
            }
        }

        NitriteMap<Key, Value> primaryMap = null;
        if (primaryStore.hasMap(mapName)) {
            primaryMap = primaryStore.openMap(mapName, keyType, valueType);
        }

        TransactionalMap<Key, Value> transactionalMap = new TransactionalMap<>(mapName, primaryMap, this);
        mapRegistry.put(mapName, transactionalMap);
        return transactionalMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String rTreeName,
                                                                               Class<?> keyType,
                                                                               Class<?> valueType) {
        if (rTreeRegistry.containsKey(rTreeName)) {
            return (NitriteRTree<Key, Value>) rTreeRegistry.get(rTreeName);
        }

        NitriteRTree<Key, Value> primaryMap = null;
        if (primaryStore.hasMap(rTreeName)) {
            primaryMap = primaryStore.openRTree(rTreeName, keyType, valueType);
        }

        TransactionalRTree<Key, Value> transactionalRtree = new TransactionalRTree<>(primaryMap);
        rTreeRegistry.put(rTreeName, transactionalRtree);
        return transactionalRtree;
    }

    @Override
    public void closeMap(String mapName) {
        mapRegistry.remove(mapName);
    }

    @Override
    public void closeRTree(String rTreeName) {
        rTreeRegistry.remove(rTreeName);
    }

    @Override
    public void removeMap(String mapName) {
        mapRegistry.remove(mapName);
        deletedMap.add(mapName);
    }

    @Override
    public void removeRTree(String rTreeName) {
        rTreeRegistry.remove(rTreeName);
        deletedMap.add(rTreeName);
    }

    @Override
    public String subscribe(StoreEventListener listener) {
        return null;
    }

    @Override
    public void unsubscribe(String subscription) {
        // nothing to do
    }

    @Override
    public String getStoreVersion() {
        return primaryStore.getStoreVersion();
    }

    @Override
    public T getStoreConfig() {
        return null;
    }
}
