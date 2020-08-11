package org.dizitart.no2.rocksdb;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.util.Iterator;

class KeySet<K> implements Iterable<K> {
    private final Marshaller marshaller;
    private final RocksDB rocksDB;
    private final ColumnFamilyHandle columnFamilyHandle;

    public KeySet(RocksDB rocksDB, ColumnFamilyHandle columnFamilyHandle, Marshaller marshaller) {
        this.rocksDB = rocksDB;
        this.columnFamilyHandle = columnFamilyHandle;
        this.marshaller = marshaller;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Iterator<K> iterator() {
        return new KeyIterator();
    }

    private class KeyIterator implements Iterator<K> {
        private final RocksIterator rawEntryIterator;

        public KeyIterator() {
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
        public K next() {
            K key = (K) marshaller.unmarshal(rawEntryIterator.key(), Object.class);
            rawEntryIterator.next();
            return key;
        }

        @Override
        protected void finalize() throws Throwable {
            rawEntryIterator.close();
            super.finalize();
        }
    }
}
