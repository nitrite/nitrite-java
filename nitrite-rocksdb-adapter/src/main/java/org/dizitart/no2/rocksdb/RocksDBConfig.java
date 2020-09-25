package org.dizitart.no2.rocksdb;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dizitart.no2.rocksdb.formatter.KryoObjectFormatter;
import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.events.StoreEventListener;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;

import java.util.HashSet;
import java.util.Set;

@Accessors(fluent = true)
public class RocksDBConfig implements StoreConfig {
    @Getter @Setter(AccessLevel.PACKAGE)
    private Set<StoreEventListener> eventListeners;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Options options;

    @Getter @Setter(AccessLevel.PACKAGE)
    private DBOptions dbOptions;

    @Getter @Setter(AccessLevel.PACKAGE)
    private ColumnFamilyOptions columnFamilyOptions;

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
    public final Boolean isReadOnly() {
        return false;
    }
}
