package org.dizitart.no2.rocksdb;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;
import org.rocksdb.util.BytewiseComparator;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;

@Slf4j
public class RocksDBMap<K, V> implements NitriteMap<K, V> {
    private final String mapName;
    private final RocksDBReference reference;
    private final RocksDBStore store;
    private AtomicLong size;

    private RocksDB rocksDB;
    private ObjectFormatter objectFormatter;
    private ColumnFamilyHandle columnFamilyHandle;
    private BytewiseComparator bytewiseComparator;

    @Getter @Setter
    private Class<?> keyType;

    @Getter @Setter
    private Class<?> valueType;

    public RocksDBMap(String mapName, RocksDBStore store,
                      RocksDBReference reference, Class<?> keyType,
                      Class<?> valueType) {
        this.mapName = mapName;
        this.reference = reference;
        this.store = store;
        this.keyType = keyType;
        this.valueType = valueType;
        initialize();
    }

    @Override
    public boolean containsKey(K k) {
        byte[] key = objectFormatter.encodeKey(k);
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
            byte[] key = objectFormatter.encodeKey(k);
            byte[] value = rocksDB.get(columnFamilyHandle, key);
            if (value == null) {
                return null;
            }

            return (V) objectFormatter.decode(value, getValueType());
        } catch (Exception e) {
            log.error("Error while querying by key", e);
            throw new NitriteIOException("failed to query by key", e);
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
        return RecordStream.fromIterable(new ValueSet<>(rocksDB, columnFamilyHandle, objectFormatter, getValueType()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(K k) {
        try {
            byte[] key = objectFormatter.encodeKey(k);

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

            return (V) objectFormatter.decode(value, getValueType());
        } catch (Exception e) {
            log.error("Error while removing key", e);
            throw new NitriteIOException("failed to remove key", e);
        }
    }

    @Override
    public RecordStream<K> keys() {
        return RecordStream.fromIterable(new KeySet<>(rocksDB, columnFamilyHandle, objectFormatter, getKeyType()));
    }

    @Override
    public void put(K k, V v) {
        notNull(v, "value cannot be null");
        try {
            byte[] key = objectFormatter.encodeKey(k);
            byte[] value = objectFormatter.encode(v);

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
            byte[] key = objectFormatter.encodeKey(k);
            byte[] oldValue = rocksDB.get(columnFamilyHandle, key);

            if (oldValue == null) {
                byte[] value = objectFormatter.encode(v);
                rocksDB.put(columnFamilyHandle, key, value);
                size.incrementAndGet();
                updateLastModifiedTime();
                return null;
            }

            return (V) objectFormatter.decode(oldValue, getValueType());
        } catch (Exception e) {
            log.error("Error while writing key and value", e);
            throw new NitriteIOException("failed to write key and value", e);
        }
    }

    @Override
    public RecordStream<Pair<K, V>> entries() {
        return RecordStream.fromIterable(new EntrySet<>(rocksDB, columnFamilyHandle,
            objectFormatter, getKeyType(), getValueType(), false));
    }

    @Override
    public RecordStream<Pair<K, V>> reversedEntries() {
        return RecordStream.fromIterable(new EntrySet<>(rocksDB, columnFamilyHandle,
            objectFormatter, getKeyType(), getValueType(), true));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public K higherKey(K k) {
        try (RocksIterator iterator = rocksDB.newIterator(columnFamilyHandle)) {
            byte[] key = objectFormatter.encodeKey(k);

            iterator.seek(key);
            if (!iterator.isValid()) {
                iterator.seekToFirst();
            }

            ByteBuffer keyBuffer = ByteBuffer.wrap(key);
            while (iterator.isValid()) {
                byte[] nextKey = iterator.key();
                ByteBuffer nextKeyBuffer = ByteBuffer.wrap(nextKey);

                if (bytewiseComparator.compare(nextKeyBuffer, keyBuffer) > 0) {
                    Comparable k2 = (Comparable) objectFormatter.decodeKey(nextKey, k.getClass());
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
            byte[] key = objectFormatter.encodeKey(k);

            iterator.seek(key);
            if (!iterator.isValid()) {
                iterator.seekToFirst();
            }

            ByteBuffer keyBuffer = ByteBuffer.wrap(key);
            while (iterator.isValid()) {
                byte[] nextKey = iterator.key();
                ByteBuffer nextKeyBuffer = ByteBuffer.wrap(nextKey);

                if (bytewiseComparator.compare(nextKeyBuffer, keyBuffer) >= 0) {
                    Comparable k2 = (Comparable) objectFormatter.decodeKey(nextKey, k.getClass());
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
            byte[] key = objectFormatter.encodeKey(k);

            iterator.seekForPrev(key);
            if (!iterator.isValid()) {
                iterator.seekToLast();
            }

            ByteBuffer keyBuffer = ByteBuffer.wrap(key);
            while (iterator.isValid()) {
                byte[] nextKey = iterator.key();
                ByteBuffer nextKeyBuffer = ByteBuffer.wrap(nextKey);

                if (bytewiseComparator.compare(nextKeyBuffer, keyBuffer) < 0) {
                    Comparable k2 = (Comparable) objectFormatter.decodeKey(nextKey, k.getClass());
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
            byte[] key = objectFormatter.encodeKey(k);

            iterator.seekForPrev(key);
            if (!iterator.isValid()) {
                iterator.seekToLast();
            }

            ByteBuffer keyBuffer = ByteBuffer.wrap(key);
            while (iterator.isValid()) {
                byte[] nextKey = iterator.key();
                ByteBuffer nextKeyBuffer = ByteBuffer.wrap(nextKey);

                if (bytewiseComparator.compare(nextKeyBuffer, keyBuffer) <= 0) {
                    Comparable k2 = (Comparable) objectFormatter.decodeKey(nextKey, k.getClass());
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
        store.removeMap(getName());
        close();
    }

    private void initialize() {
        this.size = new AtomicLong(0); // just initialized
        this.objectFormatter = store.getStoreConfig().objectFormatter();
        this.columnFamilyHandle = reference.getOrCreateColumnFamily(getName());
        this.rocksDB = reference.getRocksDB();
        this.bytewiseComparator = this.reference.getDbComparator();
    }

    @Override
    public void close() {
        store.closeMap(getName());
    }
}
