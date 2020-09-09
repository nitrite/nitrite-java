package org.dizitart.no2.rocksdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.common.UnknownType;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.AbstractNitriteStore;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.StoreMetadata;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RocksDBStore extends AbstractNitriteStore<RocksDBConfig> {
    private final AtomicBoolean closed;
    private final Map<String, NitriteMap<?, ?>> nitriteMapRegistry;
    private RocksDBReference reference;

    public RocksDBStore() {
        super();
        nitriteMapRegistry = new ConcurrentHashMap<>();
        closed = new AtomicBoolean(true);
    }

    @Override
    public void openOrCreate(String username, String password) {
        try {
            if (closed.get()) {
                this.reference = RocksDBStoreUtils.openOrCreate(username, password, getStoreConfig());
                closed.compareAndSet(true, false);
                initEventBus();
                alert(StoreEvents.Opened);
            }
        } catch (NitriteException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while opening database", e);
            throw new NitriteIOException("failed to open database", e);
        }
    }

    @Override
    public boolean isClosed() {
        return closed.get();
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
        try {
            if (!closed.get()) {
                reference.close();
                closed.compareAndSet(false, true);
            }
            alert(StoreEvents.Closed);
        } catch (Exception e) {
            log.error("Error while closing the database", e);
            throw new NitriteIOException("failed to close database", e);
        }
    }

    @Override
    public boolean hasMap(String mapName) {
        return reference.getColumnFamilyHandleRegistry().containsKey(mapName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key, Value> NitriteMap<Key, Value> openMap(String mapName,
                                                       Class<?> keyType,
                                                       Class<?> valueType) {
        if (nitriteMapRegistry.containsKey(mapName)) {
            RocksDBMap<Key, Value> nitriteMap = (RocksDBMap<Key, Value>) nitriteMapRegistry.get(mapName);
            if (UnknownType.class.equals(nitriteMap.getKeyType())) {
                nitriteMap.setKeyType(keyType);
            }

            if (UnknownType.class.equals(nitriteMap.getValueType())) {
                nitriteMap.setValueType(valueType);
            }

            return nitriteMap;
        } else {
            NitriteMap<Key, Value> nitriteMap = new RocksDBMap<>(mapName, this, this.reference, keyType, valueType);
            nitriteMapRegistry.put(mapName, nitriteMap);
            return nitriteMap;
        }
    }

    @Override
    public void removeMap(String mapName) {
        reference.dropColumnFamily(mapName);
        nitriteMapRegistry.remove(mapName);
    }

    @Override
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String rTreeName,
                                                                               Class<?> keyType,
                                                                               Class<?> valueType) {
        throw new InvalidOperationException("rtree not supported on rocksdb store");
    }

    @Override
    public void removeRTree(String mapName) {
        throw new InvalidOperationException("rtree not supported on rocksdb store");
    }

    @Override
    public StoreMetadata getStoreInfo() {
        return RocksDBStoreUtils.getStoreInfo(reference, getStoreConfig().objectFormatter());
    }

    @Override
    public void updateStoreInfo(StoreMetadata storeMetadata) {
        RocksDBStoreUtils.updateStoreInfo(reference, getStoreConfig().objectFormatter(), storeMetadata);
    }

    private void initEventBus() {
        if (getStoreConfig().eventListeners() != null) {
            for (StoreEventListener eventListener : getStoreConfig().eventListeners()) {
                eventBus.register(eventListener);
            }
        }
    }
}
