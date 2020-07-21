package org.dizitart.no2.leveldb;

import org.dizitart.no2.common.KeyValuePair;
import org.iq80.leveldb.DBIterator;
import org.nustaq.serialization.FSTConfiguration;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

class EntrySet<K, V> extends AbstractSet<KeyValuePair<K, V>> {
    private final FSTConfiguration configuration;
    private final LevelDBMap<?, ?> levelDBMap;
    private final SubLevelDB db;

    public EntrySet(SubLevelDB db, LevelDBMap<?, ?> levelDBMap, FSTConfiguration configuration) {
        this.db = db;
        this.levelDBMap = levelDBMap;
        this.configuration = configuration;
    }

    @Override
    public Iterator<KeyValuePair<K, V>> iterator() {
        return new EntryIterator();
    }

    @Override
    public int size() {
        return (int) levelDBMap.size();
    }

    private class EntryIterator implements Iterator<KeyValuePair<K, V>> {
        private final DBIterator rawEntryIterator = db.iterator();

        @Override
        public boolean hasNext() {
            return rawEntryIterator.hasNext();
        }

        @Override
        @SuppressWarnings("unchecked")
        public KeyValuePair<K, V> next() {
            Map.Entry<byte[], byte[]> rawEntry = rawEntryIterator.next();
            K key = (K) configuration.asObject(rawEntry.getKey());
            V value = (V) configuration.asObject(rawEntry.getValue());
            return new KeyValuePair<>(key, value);
        }

        @Override
        public void remove() {
            rawEntryIterator.remove();
        }
    }
}
