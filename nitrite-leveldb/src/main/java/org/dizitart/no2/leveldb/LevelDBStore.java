package org.dizitart.no2.leveldb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.*;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;
import org.iq80.leveldb.DB;
import org.nustaq.serialization.FSTConfiguration;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class LevelDBStore extends AbstractNitriteStore {
    private static final FSTConfiguration conf;
    private final AtomicBoolean closed;
    private final Map<String, NitriteMap<?, ?>> nitriteMapRegistry;
    private DB db;
    private LevelDBStoreConfig storeConfig;

    static {
        conf = FSTConfiguration.createDefaultConfiguration();
    }

    public LevelDBStore() {
        super();
        closed = new AtomicBoolean(true);
        nitriteMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void openOrCreate(String username, String password, StoreConfig storeConfig) {
        try {
            validateStoreConfig(storeConfig);
            this.storeConfig = (LevelDBStoreConfig) storeConfig;
            if (closed.get()) {
                this.db = LevelDBStoreUtils.openOrCreate(username, password, this.storeConfig);
                closed.compareAndSet(true, false);
                initEventBus();
                alert(StoreEvents.Opened);
            }
        } catch (Exception e) {
            log.error("Error while opening database", e);
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
                db.close();
                closed.compareAndSet(false, true);
            }
            alert(StoreEvents.Closed);
        } catch (IOException e) {
            log.error("Error while closing the database", e);
            throw new NitriteIOException("failed to close database", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key, Value> NitriteMap<Key, Value> openMap(String mapName) {
        if (nitriteMapRegistry.containsKey(mapName)) {
            return (NitriteMap<Key, Value>) nitriteMapRegistry.get(mapName);
        } else {
            SubLevelDB subLevelDB = new SubLevelDB(this.db, mapName);
            NitriteMap<Key, Value> nitriteMap = new LevelDBMap<>(subLevelDB, conf, this);
            nitriteMapRegistry.put(mapName, nitriteMap);
            return nitriteMap;
        }
    }

    @Override
    public void removeMap(String mapName) {
        nitriteMapRegistry.remove(mapName);
    }

    @Override
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String rTreeName) {
        throw new InvalidOperationException("rtree not supported on leveldb store");
    }

    @Override
    public StoreInfo getStoreInfo() {
        return LevelDBStoreUtils.getStoreInfo(this);
    }

    private void validateStoreConfig(StoreConfig storeConfig) {
        if (!(storeConfig instanceof LevelDBStoreConfig)) {
            throw new ValidationException("store config is not valid mv store config");
        }
    }

    private void initEventBus() {
        if (storeConfig.getEventListeners() != null) {
            for (StoreEventListener eventListener : storeConfig.getEventListeners()) {
                eventBus.register(eventListener);
            }
        }
    }
}
