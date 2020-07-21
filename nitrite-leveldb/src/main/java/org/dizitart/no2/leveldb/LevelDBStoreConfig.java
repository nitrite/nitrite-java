package org.dizitart.no2.leveldb;

import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.List;

public class LevelDBStoreConfig implements StoreConfig {
    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void addStoreEventListener(StoreEventListener listener) {

    }

    public List<StoreEventListener> getEventListeners() {
        return false;
    }
}
