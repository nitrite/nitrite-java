package org.dizitart.no2.rocksdb;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dizitart.no2.rocksdb.serializers.kyro.KryoObjectSerializer;
import org.dizitart.no2.rocksdb.serializers.ObjectSerializer;
import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.events.StoreEventListener;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration class for RocksDB store. It implements the {@link StoreConfig}
 * interface.
 * 
 * @since 4.0
 * @see StoreConfig
 * @see RocksDBModule
 * @see RocksDBModuleBuilder
 * @author Anindya Chatterjee
 */
@Accessors(fluent = true)
public class RocksDBConfig implements StoreConfig {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    /**
     * The set of event listeners registered with this configuration.
     */
    private Set<StoreEventListener> eventListeners;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    /**
     * The RocksDB {@link Options} used to configure the database instance.
     */
    private Options options;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    /**
     * The RocksDB {@link DBOptions} used to configure the database instance.
     */
    private DBOptions dbOptions;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    /**
     * The RocksDB {@link ColumnFamilyOptions} used to configure the database instance.
     */
    private ColumnFamilyOptions columnFamilyOptions;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    /**
     * The file path of the RocksDB data store.
     */
    private String filePath;

    /**
     * @param objectSerializer
     *
     * @deprecated  reason this method is deprecated <br/>
     *              {will be removed in next version} <br/>
     *              use {@link #objectSerializer()} ()} instead like this:
     *
     *
     * <blockquote><pre>
     * RocksDBModule rocksDBStorageModule = RocksDBModule.withConfig()
     *                 .filePath(newDirectory)
     *                 .objectSerializer(new FuryObjectSerializer())
     *                 .build();
     * </pre></blockquote>
     *
     */
    @Deprecated(since = "4.3.0", forRemoval = true)
    private void objectFormatter(ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }


    @Getter
    @Setter(AccessLevel.PACKAGE)
    /**
     * The object formatter used to serialize and deserialize objects.
     */
    private ObjectSerializer objectSerializer;

    RocksDBConfig() {
        eventListeners = new HashSet<>();
        objectSerializer = new KryoObjectSerializer();
    }

    /**
     * Adds a store event listener to the configuration.
     *
     * @param listener the store event listener to add
     */
    @Override
    public void addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
    }

    /**
     * Returns whether the RocksDB instance is in memory or not.
     *
     * @return {@code true} if the RocksDB instance is in memory; {@code false} otherwise.
     */
    @Override
    public final boolean isInMemory() {
        return false;
    }

    @Override
    public final Boolean isReadOnly() {
        return false;
    }
}
