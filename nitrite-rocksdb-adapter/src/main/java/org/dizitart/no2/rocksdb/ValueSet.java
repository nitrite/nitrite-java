package org.dizitart.no2.rocksdb;

import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.util.Iterator;

class ValueSet<V> implements Iterable<V> {
    private final ObjectFormatter objectFormatter;
    private final RocksDB rocksDB;
    private final ColumnFamilyHandle columnFamilyHandle;
    private final Class<?> valueType;

    public ValueSet(RocksDB rocksDB, ColumnFamilyHandle columnFamilyHandle, ObjectFormatter objectFormatter, Class<?> valueType) {
        this.rocksDB = rocksDB;
        this.columnFamilyHandle = columnFamilyHandle;
        this.objectFormatter = objectFormatter;
        this.valueType = valueType;
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
            return (V) objectFormatter.decode(value, valueType);
        }

        @Override
        protected void finalize() throws Throwable {
            rawEntryIterator.close();
            super.finalize();
        }
    }
}
