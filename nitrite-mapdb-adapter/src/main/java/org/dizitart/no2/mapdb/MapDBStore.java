package org.dizitart.no2.mapdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.mapdb.serializers.Serializers;
import org.dizitart.no2.store.AbstractNitriteStore;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.serializer.GroupSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class MapDBStore extends AbstractNitriteStore<MapDBConfig> {
    private DB db;
    private final Map<String, NitriteMap<?, ?>> nitriteMapRegistry;

    public MapDBStore() {
        super();
        nitriteMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void openOrCreate() {
        this.db = StoreFactory.open(getStoreConfig());
        initEventBus();
        alert(StoreEvents.Opened);
    }

    @Override
    public boolean isClosed() {
        return db == null || db.isClosed();
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return db.getStore().isReadOnly();
    }

    @Override
    public void commit() {
        db.commit();
        alert(StoreEvents.Commit);
    }

    @Override
    public void close() throws Exception {
        db.close();

        for (NitriteMap<?, ?> nitriteMap : nitriteMapRegistry.values()) {
            nitriteMap.close();
        }

        alert(StoreEvents.Closed);
    }

    @Override
    public boolean hasMap(String mapName) {
        return db.exists(mapName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key, Value> NitriteMap<Key, Value> openMap(String mapName, Class<?> keyType, Class<?> valueType) {
        if (nitriteMapRegistry.containsKey(mapName)) {
            return (MapDBMap<Key, Value>) nitriteMapRegistry.get(mapName);
        }

        GroupSerializer<?> keySerializer = Serializers.findSerializer(keyType);
        GroupSerializer<?> valueSerializer = Serializers.findSerializer(valueType);

        DB.TreeMapMaker<Key, Value> treeMapMaker = (DB.TreeMapMaker<Key, Value>) db.treeMap(mapName)
            .counterEnable()
            .valuesOutsideNodesEnable();

        if (keySerializer != null) {
            treeMapMaker.keySerializer(keySerializer);
        }

        if (valueSerializer != null) {
            treeMapMaker.valueSerializer(valueSerializer);
        }

        BTreeMap<Key, Value> bTreeMap = treeMapMaker.createOrOpen();

        // mapdb btreemap does not support null key, so all null key entries are maintained in a separate map
        DB.TreeMapMaker<DBNull, Value> nullMapMaker = (DB.TreeMapMaker<DBNull, Value>) db
            .treeMap(mapName + "null-map")
            .counterEnable()
            .valuesOutsideNodesEnable();

        if (valueSerializer != null) {
            nullMapMaker.valueSerializer(valueSerializer);
        }

        BTreeMap<DBNull, Value> nullMap = nullMapMaker.createOrOpen();

        MapDBMap<Key, Value> mapDBMap = new MapDBMap<>(mapName, bTreeMap, nullMap, this);
        nitriteMapRegistry.put(mapName, mapDBMap);
        return mapDBMap;
    }

    @Override
    public void removeMap(String mapName) {
        if (nitriteMapRegistry.containsKey(mapName)) {
            MapDBMap<?, ?> mapDb = (MapDBMap<?, ?>) nitriteMapRegistry.get(mapName);
            BTreeMap<?, ?> bTreeMap = mapDb.getBTreeMap();
            BTreeMap<?, ?> nullEntryMap = mapDb.getNullEntryMap();

            bTreeMap.clear();
            nullEntryMap.clear();

            nitriteMapRegistry.remove(mapName);
        }
    }

    @Override
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String rTreeName, Class<?> keyType, Class<?> valueType) {
        throw new InvalidOperationException("rtree not supported on mapdb store");
    }

    @Override
    public void removeRTree(String mapName) {
        throw new InvalidOperationException("rtree not supported on mapdb store");
    }

    @Override
    public String getStoreVersion() {
        return "MapDB/" + getMapDbVersion();
    }

    private void initEventBus() {
        if (getStoreConfig().eventListeners() != null) {
            for (StoreEventListener eventListener : getStoreConfig().eventListeners()) {
                eventBus.register(eventListener);
            }
        }
    }

    private static String getMapDbVersion() {
        return "3.0.8";
    }
}
