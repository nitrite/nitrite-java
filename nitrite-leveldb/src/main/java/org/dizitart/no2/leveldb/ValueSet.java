package org.dizitart.no2.leveldb;

import org.iq80.leveldb.DBIterator;
import org.nustaq.serialization.FSTConfiguration;

import java.util.AbstractCollection;
import java.util.Iterator;

class ValueSet<V> extends AbstractCollection<V> {
    private final FSTConfiguration configuration;
    private final LevelDBMap<?, ?> levelDBMap;
    private final SubLevelDB db;

    public ValueSet(SubLevelDB db, LevelDBMap<?, ?> levelDBMap, FSTConfiguration configuration) {
        this.db = db;
        this.levelDBMap = levelDBMap;
        this.configuration = configuration;
    }

    @Override
    public Iterator<V> iterator() {
        return new ValueIterator();
    }

    @Override
    public int size() {
        return (int) levelDBMap.size();
    }

    private class ValueIterator implements Iterator<V> {
        private final DBIterator rawEntryIterator = db.iterator();

        @Override
        public boolean hasNext() {
            return rawEntryIterator.hasNext();
        }

        @Override
        @SuppressWarnings("unchecked")
        public V next() {
            byte[] value = rawEntryIterator.next().getValue();
            return (V) configuration.asObject(value);
        }

        @Override
        public void remove() {
            rawEntryIterator.remove();
        }
    }
}
