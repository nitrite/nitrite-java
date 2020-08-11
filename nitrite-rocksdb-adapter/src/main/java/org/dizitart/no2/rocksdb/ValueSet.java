package org.dizitart.no2.rocksdb;

import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.util.Iterator;

class ValueSet<V> implements Iterable<V> {
    private final Marshaller marshaller;
    private final RocksDB rocksDB;
    private final ColumnFamilyHandle columnFamilyHandle;

    public ValueSet(RocksDB rocksDB, ColumnFamilyHandle columnFamilyHandle, Marshaller marshaller) {
        this.rocksDB = rocksDB;
        this.columnFamilyHandle = columnFamilyHandle;
        this.marshaller = marshaller;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Iterator<V> iterator() {
        return new ValueIterator();
    }

    private class ValueIterator implements Iterator<V> {
        private final RocksIterator rawEntryIterator;

        public ValueIterator() {
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
        public V next() {
            byte[] value = rawEntryIterator.value();
            rawEntryIterator.next();
            return (V) marshaller.unmarshal(value, Object.class);
        }

        @Override
        protected void finalize() throws Throwable {
            rawEntryIterator.close();
            super.finalize();
        }
    }
}
