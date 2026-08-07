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
class KeySet<K> implements Iterable<K> {
    private final ObjectFormatter objectFormatter;
    private final RocksDB rocksDB;
    private final ColumnFamilyHandle columnFamilyHandle;
    private final Class<?> keyType;

    public KeySet(RocksDB rocksDB, ColumnFamilyHandle columnFamilyHandle, ObjectFormatter objectFormatter, Class<?> keyType) {
        this.rocksDB = rocksDB;
        this.columnFamilyHandle = columnFamilyHandle;
        this.objectFormatter = objectFormatter;
        this.keyType = keyType;
    }

    @Override
    public Iterator<K> iterator() {
        return new KeyIterator();
    }

    private class KeyIterator implements Iterator<K>, AutoCloseable {
        private final RocksIterator rawEntryIterator;
        private final Cleaner.Cleanable cleanable;

        public KeyIterator() {
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
        public K next() {
            K key = (K) objectFormatter.decodeKey(rawEntryIterator.key(), keyType);
            rawEntryIterator.next();
            return key;
        }

        @Override
        public void close() {
            cleanable.clean();
        }
    }
}
