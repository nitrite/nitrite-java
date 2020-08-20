package org.dizitart.no2.rocksdb;

import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.rocksdb.*;

import java.util.Iterator;

class EntrySet<K, V> implements Iterable<KeyValuePair<K, V>> {
    private final ObjectFormatter objectFormatter;
    private final RocksDB rocksDB;
    private final ColumnFamilyHandle columnFamilyHandle;
    private final Class<?> keyType;
    private final Class<?> valueType;

    public EntrySet(RocksDB rocksDB, ColumnFamilyHandle columnFamilyHandle,
                    ObjectFormatter objectFormatter, Class<?> keyType, Class<?> valueType) {
        this.rocksDB = rocksDB;
        this.columnFamilyHandle = columnFamilyHandle;
        this.objectFormatter = objectFormatter;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Iterator<KeyValuePair<K, V>> iterator() {
        return new EntryIterator();
    }

    private class EntryIterator implements Iterator<KeyValuePair<K, V>> {
        private final RocksIterator rawEntryIterator;

        public EntryIterator() {
            rawEntryIterator = rocksDB.newIterator(columnFamilyHandle);
            rawEntryIterator.seekToFirst();
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
        public KeyValuePair<K, V> next() {
            K key = (K) objectFormatter.decodeKey(rawEntryIterator.key(), keyType);
            try {
                V value = (V) objectFormatter.decode(rawEntryIterator.value(), valueType);
                rawEntryIterator.next();
                return new KeyValuePair<>(key, value);
            } catch (Exception e) {
                System.out.println(new String(rawEntryIterator.value()));
                throw e;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            rawEntryIterator.close();
            super.finalize();
        }
    }
}
