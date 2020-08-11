package org.dizitart.no2.rocksdb;

import org.dizitart.no2.common.KeyValuePair;
import org.rocksdb.*;

import java.util.Iterator;

class EntrySet<K, V> implements Iterable<KeyValuePair<K, V>> {
    private final Marshaller marshaller;
    private final RocksDB rocksDB;
    private final ColumnFamilyHandle columnFamilyHandle;

    public EntrySet(RocksDB rocksDB, ColumnFamilyHandle columnFamilyHandle, Marshaller marshaller) {
        this.rocksDB = rocksDB;
        this.columnFamilyHandle = columnFamilyHandle;
        this.marshaller = marshaller;
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
            K key = (K) marshaller.unmarshal(rawEntryIterator.key(), Object.class);
            V value = (V) marshaller.unmarshal(rawEntryIterator.value(), Object.class);
            rawEntryIterator.next();
            return new KeyValuePair<>(key, value);
        }

        @Override
        protected void finalize() throws Throwable {
            rawEntryIterator.close();
            super.finalize();
        }
    }
}
