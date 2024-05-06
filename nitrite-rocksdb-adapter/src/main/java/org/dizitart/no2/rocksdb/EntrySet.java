package org.dizitart.no2.rocksdb;

import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.rocksdb.serializers.ObjectSerializer;
import org.rocksdb.*;

import java.util.Iterator;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
class EntrySet<K, V> implements Iterable<Pair<K, V>> {
    private final ObjectSerializer objectSerializer;
    private final RocksDB rocksDB;
    private final ColumnFamilyHandle columnFamilyHandle;
    private final Class<?> keyType;
    private final Class<?> valueType;
    private final boolean reverse;

    public EntrySet(RocksDB rocksDB, ColumnFamilyHandle columnFamilyHandle,
                    ObjectSerializer objectSerializer, Class<?> keyType,
                    Class<?> valueType, boolean reverse) {
        this.rocksDB = rocksDB;
        this.columnFamilyHandle = columnFamilyHandle;
        this.objectSerializer = objectSerializer;
        this.keyType = keyType;
        this.valueType = valueType;
        this.reverse = reverse;
    }

    @Override
    public Iterator<Pair<K, V>> iterator() {
        return new EntryIterator();
    }

    private class EntryIterator implements Iterator<Pair<K, V>> {
        private final RocksIterator rawEntryIterator;

        public EntryIterator() {
            rawEntryIterator = rocksDB.newIterator(columnFamilyHandle);
            if (reverse) {
                rawEntryIterator.seekToLast();
            } else {
                rawEntryIterator.seekToFirst();
            }
        }

        @Override
        public boolean hasNext() {
            try {
                boolean result = rawEntryIterator.isValid();
                if (!result) {
                    rawEntryIterator.close();
                }
                return result;
            } catch (AssertionError e) {
                return false;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Pair<K, V> next() {
            K key = (K) objectSerializer.decodeKey(rawEntryIterator.key(), keyType);
            V value = (V) objectSerializer.decode(rawEntryIterator.value(), valueType);
            if (reverse) {
                rawEntryIterator.prev();
            } else {
                rawEntryIterator.next();
            }
            return new Pair<>(key, value);
        }
    }
}
