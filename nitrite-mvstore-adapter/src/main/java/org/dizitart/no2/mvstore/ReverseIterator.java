package org.dizitart.no2.mvstore;

import org.dizitart.no2.common.tuples.Pair;
import org.h2.mvstore.MVMap;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
public class ReverseIterator<Key, Value> implements Iterator<Pair<Key, Value>> {
    private final MVMap<Key, Value> mvMap;
    private Key anchor;
    private boolean started;

    public ReverseIterator(MVMap<Key, Value> mvMap) {
        long version = mvMap.getVersion();
        this.mvMap = mvMap.openVersion(version);
        this.anchor = this.mvMap.lastKey();
        this.started = false;
    }

    @Override
    public boolean hasNext() {
        Key key = started ? mvMap.lowerKey(this.anchor) : mvMap.floorKey(this.anchor);
        return key != null;
    }

    @Override
    public Pair<Key, Value> next() {
        Key key = started ? mvMap.lowerKey(this.anchor) : mvMap.floorKey(this.anchor);
        this.started = true;
        if (key == null) {
            throw new NoSuchElementException();
        }

        Value value = mvMap.get(key);
        this.anchor = key;
        return new Pair<>(key, value);
    }
}
