package org.dizitart.no2.rocksdb;

import org.dizitart.no2.common.streams.SkippableIterator;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.rocksdb.*;

import java.util.Iterator;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
class EntrySet<K, V> implements Iterable<Pair<K, V>> {
    private final ObjectFormatter objectFormatter;
    private final RocksDB rocksDB;
    private final ColumnFamilyHandle columnFamilyHandle;
    private final Class<?> keyType;
    private final Class<?> valueType;
    private final boolean reverse;

    public EntrySet(RocksDB rocksDB, ColumnFamilyHandle columnFamilyHandle,
                    ObjectFormatter objectFormatter, Class<?> keyType,
                    Class<?> valueType, boolean reverse) {
        this.rocksDB = rocksDB;
        this.columnFamilyHandle = columnFamilyHandle;
        this.objectFormatter = objectFormatter;
        this.keyType = keyType;
        this.valueType = valueType;
        this.reverse = reverse;
    }

    @Override
    public Iterator<Pair<K, V>> iterator() {
        return new EntryIterator();
    }

    private class EntryIterator implements Iterator<Pair<K, V>>, SkippableIterator {
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

        /**
         * RocksDB has no seek-by-index, so this still steps the iterator once per skipped record -
         * but it steps it natively and never calls {@code value()} or the formatter. The decode of
         * a document is what a skipped row actually costs; advancing past one is a memcmp inside
         * the SST block the iterator is already positioned on.
         */
        @Override
        public long skip(long count) {
            long skipped = 0;
            while (skipped < count && rawEntryIterator.isOwningHandle() && rawEntryIterator.isValid()) {
                if (reverse) {
                    rawEntryIterator.prev();
                } else {
                    rawEntryIterator.next();
                }
                skipped++;
            }
            return skipped;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Pair<K, V> next() {
            K key = (K) objectFormatter.decodeKey(rawEntryIterator.key(), keyType);
            V value = (V) objectFormatter.decode(rawEntryIterator.value(), valueType);
            if (reverse) {
                rawEntryIterator.prev();
            } else {
                rawEntryIterator.next();
            }
            return new Pair<>(key, value);
        }
    }
}
