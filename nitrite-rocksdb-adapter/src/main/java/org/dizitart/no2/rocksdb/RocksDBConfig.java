package org.dizitart.no2.rocksdb;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dizitart.no2.rocksdb.formatter.KryoObjectFormatter;
import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.HashSet;
import java.util.Set;

@Accessors(fluent = true)
public class RocksDBConfig implements StoreConfig {
    @Getter @Setter(AccessLevel.PACKAGE)
    private Set<StoreEventListener> eventListeners;

    @Getter @Setter(AccessLevel.PACKAGE)
    private boolean createIfMissing = true;

    @Getter @Setter(AccessLevel.PACKAGE)
    private boolean errorIfExists;

    @Getter @Setter(AccessLevel.PACKAGE)
    private int writeBufferSize = 4 << 20;

    @Getter @Setter(AccessLevel.PACKAGE)
    private int maxOpenFiles = 1000;

    @Getter @Setter(AccessLevel.PACKAGE)
    private boolean paranoidChecks;

    @Getter @Setter(AccessLevel.PACKAGE)
    private String filePath;

    @Getter @Setter(AccessLevel.PACKAGE)
    private ObjectFormatter objectFormatter;


    RocksDBConfig() {
        eventListeners = new HashSet<>();
        objectFormatter = new KryoObjectFormatter();
    }

    @Override
    public void addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
    }

    @Override
    public final boolean isInMemory() {
        return false;
    }

    @Override
    public final boolean isReadOnly() {
        return false;
    }
}
