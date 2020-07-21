package org.dizitart.no2.leveldb;

import org.iq80.leveldb.DBIterator;
import org.nustaq.serialization.FSTConfiguration;

import java.util.AbstractSet;
import java.util.Iterator;

class KeySet<K> extends AbstractSet<K> {
    private final FSTConfiguration configuration;
    private final LevelDBMap<?, ?> levelDBMap;
    private final SubLevelDB db;

    public KeySet(SubLevelDB db, LevelDBMap<?, ?> levelDBMap, FSTConfiguration configuration) {
        this.db = db;
        this.levelDBMap = levelDBMap;
        this.configuration = configuration;
    }

    @Override
    public Iterator<K> iterator() {
        return new KeyIterator();
    }

    @Override
    public int size() {
        return (int) levelDBMap.size();
    }

    private class KeyIterator implements Iterator<K> {
        private final DBIterator rawEntryIterator = db.iterator();

        @Override
        public boolean hasNext() {
            return rawEntryIterator.hasNext();
        }

        @Override
        @SuppressWarnings("unchecked")
        public K next() {
            byte[] key = rawEntryIterator.next().getKey();
            return (K) configuration.asObject(key);
        }

        @Override
        public void remove() {
            rawEntryIterator.remove();
        }
    }
}
