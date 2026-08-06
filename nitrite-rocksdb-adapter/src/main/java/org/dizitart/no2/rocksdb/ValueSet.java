package org.dizitart.no2.rocksdb;

import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.lang.ref.Cleaner;
import java.util.Iterator;

import static org.dizitart.no2.rocksdb.Constants.CLEANER;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
class ValueSet<V> implements Iterable<V> {
    private final ObjectFormatter objectFormatter;
    private final RocksDB rocksDB;
    private final ColumnFamilyHandle columnFamilyHandle;
    private final Class<?> valueType;

    public ValueSet(RocksDB rocksDB, ColumnFamilyHandle columnFamilyHandle,
                    ObjectFormatter objectFormatter, Class<?> valueType) {
        this.rocksDB = rocksDB;
        this.columnFamilyHandle = columnFamilyHandle;
        this.objectFormatter = objectFormatter;
        this.valueType = valueType;
    }

    @Override
    public Iterator<V> iterator() {
        return new ValueIterator();
    }

    private class ValueIterator implements Iterator<V>, AutoCloseable {
        private final RocksIterator rawEntryIterator;
        private final Cleaner.Cleanable cleanable;

        public ValueIterator() {
            rawEntryIterator = rocksDB.newIterator(columnFamilyHandle);
            rawEntryIterator.seekToFirst();
            cleanable = CLEANER.register(this, new CleaningAction(rawEntryIterator));
        }

        @Override
        public boolean hasNext() {
            // hasNext() is idempotent by contract, and this one closes the native iterator the
            // first time it answers false. Asking again then reaches isValid() on a freed handle:
            // an AssertionError with -ea, and a SIGSEGV without it. BoundedStream's skip loop
            // exhausts the iterator and the caller asks once more, so any find(skipBy(n)) past the
            // end of a collection reaches it — with assertions off, which is how an application
            // actually runs.
            if (!rawEntryIterator.isOwningHandle()) {
                return false;
            }
            boolean result = rawEntryIterator.isValid();
            if (!result) {
                rawEntryIterator.close();
            }
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V next() {
            byte[] value = rawEntryIterator.value();
            rawEntryIterator.next();
            return (V) objectFormatter.decode(value, valueType);
        }

        @Override
        public void close() {
            cleanable.clean();
        }
    }
}
