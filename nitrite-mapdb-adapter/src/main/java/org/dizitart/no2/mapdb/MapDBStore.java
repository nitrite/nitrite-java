package org.dizitart.no2.mapdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.AbstractNitriteStore;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.StoreInfo;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class MapDBStore extends AbstractNitriteStore<MapDBConfig> {

    public MapDBStore() {
        super();
    }

    @Override
    public void openOrCreate(String username, String password) {

    }

    @Override
    public boolean isClosed() {
        return false;
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

    }

    @Override
    public void close() {

    }

    @Override
    public <Key, Value> NitriteMap<Key, Value> openMap(String mapName, Class<?> keyType, Class<?> valueType) {
        return null;
    }

    @Override
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String rTreeName, Class<?> keyType, Class<?> valueType) {
        return null;
    }

    @Override
    public StoreInfo getStoreInfo() {
        return null;
    }
}
