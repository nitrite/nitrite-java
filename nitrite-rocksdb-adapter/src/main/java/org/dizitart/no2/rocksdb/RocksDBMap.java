package org.dizitart.no2.rocksdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.util.concurrent.atomic.AtomicLong;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.rocksdb.Constants.DB_NULL;

@Slf4j
public class RocksDBMap<K, V> implements NitriteMap<K, V> {
    private final String mapName;
    private final RocksDBReference reference;
    private final RocksDBStore store;
    private AtomicLong size;

    private RocksDB rocksDB;
    private Marshaller marshaller;
    private ColumnFamilyHandle columnFamilyHandle;

    public RocksDBMap(String mapName, RocksDBStore store, RocksDBReference reference) {
        this.mapName = mapName;
        this.reference = reference;
        this.store = store;
        initialize();
    }

    @Override
    public boolean containsKey(K k) {
        byte[] key = marshaller.marshal(k);
        try {
            // check if key definitely does not exist, then return false
            boolean result = rocksDB.keyMayExist(columnFamilyHandle, key, null);
            if (!result) return false;

            // if above result is true then double check if really the key exists
            return rocksDB.get(columnFamilyHandle, key) != null;
        } catch (Exception e) {
            log.error("Error while querying key", e);
            throw new NitriteIOException("failed to check key", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K k) {
        try {
            byte[] key = marshaller.marshal(k);
            byte[] value = rocksDB.get(columnFamilyHandle, key);
            if (value == null) {
                return null;
            }

            return (V) marshaller.unmarshal(value, Object.class);
        } catch (Exception e) {
            log.error("Error while querying key", e);
            throw new NitriteIOException("failed to query key", e);
        }
    }

    @Override
    public NitriteStore<?> getStore() {
        return store;
    }

    @Override
    public void clear() {
        // drop and recreate column family and reset the size counter
        reference.dropColumnFamily(mapName);
        columnFamilyHandle = reference.getOrCreateColumnFamily(mapName);
        size.set(0L);
        updateLastModifiedTime();
    }

    @Override
    public String getName() {
        return mapName;
    }

    @Override
    public RecordStream<V> values() {
        return RecordStream.fromIterable(new ValueSet<>(rocksDB, columnFamilyHandle, marshaller));
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(K k) {
        try {
            byte[] key = marshaller.marshal(k);

            // if the definitely does not exists return null
            if (!rocksDB.keyMayExist(columnFamilyHandle, key, null)) {
                return null;
            }

            // double check if the key exists, if does not return null
            byte[] value = reference.getRocksDB().get(columnFamilyHandle, key);
            if (value == null) {
                return null;
            }

            // if key exists with null value, delete the key and return null
            reference.getRocksDB().delete(columnFamilyHandle, key);
            size.decrementAndGet();
            updateLastModifiedTime();

            return (V) marshaller.unmarshal(value, Object.class);
        } catch (Exception e) {
            log.error("Error while removing key", e);
            throw new NitriteIOException("failed to remove key", e);
        }
    }

    @Override
    public RecordStream<K> keySet() {
        return RecordStream.fromIterable(new KeySet<>(rocksDB, columnFamilyHandle, marshaller));
    }

    @Override
    public void put(K k, V v) {
        notNull(v, "value cannot be null");
        try {
            byte[] key = k == null ? DB_NULL : marshaller.marshal(k);
            byte[] value = marshaller.marshal(v);

            // check if this is update or insert
            boolean result = rocksDB.keyMayExist(columnFamilyHandle, key, null);

            reference.getRocksDB().put(columnFamilyHandle, key, value);
            if (!result) {
                // if insert then update the size
                size.incrementAndGet();
            }

            updateLastModifiedTime();
        } catch (Exception e) {
            log.error("Error while writing key and value for " + mapName, e);
            throw new NitriteIOException("failed to write key and value", e);
        }
    }

    @Override
    public long size() {
        if (size.get() == 0) {
            // first time size calculation after db opening

            try (RocksIterator iterator = rocksDB.newIterator(columnFamilyHandle)) {
                iterator.seekToFirst();

                while (iterator.isValid()) {
                    size.incrementAndGet();
                    iterator.next();
                }
            }
        }

        // calculation already done and counter already started
        return size.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public V putIfAbsent(K k, V v) {
        notNull(v, "value cannot be null");

        try {
            byte[] key = k == null ? DB_NULL : marshaller.marshal(k);
            byte[] oldValue = rocksDB.get(columnFamilyHandle, key);

            if (oldValue == null) {
                byte[] value = marshaller.marshal(v);
                rocksDB.put(columnFamilyHandle, key, value);
                size.incrementAndGet();
                updateLastModifiedTime();
                return null;
            }

            return (V) marshaller.unmarshal(oldValue, Object.class);
        } catch (Exception e) {
            log.error("Error while writing key and value", e);
            throw new NitriteIOException("failed to write key and value", e);
        }
    }

    @Override
    public RecordStream<KeyValuePair<K, V>> entries() {
        return RecordStream.fromIterable(new EntrySet<>(rocksDB, columnFamilyHandle, marshaller));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public K higherKey(K k) {
        try (RocksIterator iterator = rocksDB.newIterator(columnFamilyHandle)) {
            byte[] key = marshaller.marshal(k);

            iterator.seek(key);
            if (!iterator.isValid()) {
                iterator.seekToFirst();
            }

            while (iterator.isValid()) {
                byte[] nextKey = iterator.key();

                Comparable k2 = marshaller.unmarshal(nextKey, Comparable.class);
                if (k2.compareTo(k) > 0) {
                    return (K) k2;
                }
                iterator.next();
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public K ceilingKey(K k) {
        try (RocksIterator iterator = rocksDB.newIterator(columnFamilyHandle)) {
            byte[] key = marshaller.marshal(k);

            iterator.seek(key);
            if (!iterator.isValid()) {
                iterator.seekToFirst();
            }

            while (iterator.isValid()) {
                byte[] nextKey = iterator.key();

                Comparable k2 = marshaller.unmarshal(nextKey, Comparable.class);
                if (k2.compareTo(k) >= 0) {
                    return (K) k2;
                }
                iterator.next();
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public K lowerKey(K k) {
        try (RocksIterator iterator = rocksDB.newIterator(columnFamilyHandle)) {
            byte[] key = marshaller.marshal(k);

            iterator.seek(key);
            if (!iterator.isValid()) {
                iterator.seekToLast();
            }

            while (iterator.isValid()) {
                byte[] nextKey = iterator.key();

                Comparable k2 = marshaller.unmarshal(nextKey, Comparable.class);
                if (k2.compareTo(k) < 0) {
                    return (K) k2;
                }

                iterator.prev();
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public K floorKey(K k) {
        try (RocksIterator iterator = rocksDB.newIterator(columnFamilyHandle)) {
            byte[] key = marshaller.marshal(k);

            iterator.seek(key);
            if (!iterator.isValid()) {
                iterator.seekToLast();
            }

            while (iterator.isValid()) {
                byte[] nextKey = iterator.key();

                Comparable k2 = marshaller.unmarshal(nextKey, Comparable.class);
                if (k2.compareTo(k) <= 0) {
                    return (K) k2;
                }

                iterator.prev();
            }
        }

        return null;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void drop() {
        store.removeMap(mapName);
    }

    private void initialize() {
        this.size = new AtomicLong(0); // just initialized
        this.marshaller = store.getStoreConfig().marshaller();
        this.columnFamilyHandle = reference.getOrCreateColumnFamily(getName());
        this.rocksDB = reference.getRocksDB();
    }
}
