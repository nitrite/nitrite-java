/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.mvstore;

import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.streams.SkippableIterator;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.h2.mvstore.Cursor;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @since 1.0
 * @author Anindya Chatterjee
 */
class NitriteMVMap<Key, Value> implements NitriteMap<Key, Value> {
    private final MVMap<Key, Value> mvMap;
    private final NitriteStore<?> nitriteStore;
    private final MVStore mvStore;
    private final AtomicBoolean droppedFlag;
    private final AtomicBoolean closedFlag;

    NitriteMVMap(MVMap<Key, Value> mvMap, NitriteStore<?> nitriteStore) {
        this.mvMap = mvMap;
        this.nitriteStore = nitriteStore;
        this.mvStore = mvMap.getStore();
        this.closedFlag = new AtomicBoolean(false);
        this.droppedFlag = new AtomicBoolean(false);
    }

    @Override
    public boolean containsKey(Key key) {
        return mvMap.containsKey(key);
    }

    @Override
    public Value get(Key key) {
        return mvMap.get(key);
    }

    @Override
    public NitriteStore<?> getStore() {
        return nitriteStore;
    }

    @Override
    public void clear() {
        MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
        try {
            mvMap.clear();
            updateLastModifiedTime();
        } finally {
            mvStore.deregisterVersionUsage(txCounter);
        }
    }

    @Override
    public String getName() {
        return mvMap.getName();
    }

    @Override
    public RecordStream<Value> values() {
        return RecordStream.fromIterable(mvMap.values());
    }

    @Override
    public Value remove(Key key) {
        MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
        try {
            Value value = mvMap.remove(key);
            updateLastModifiedTime();
            return value;
        } finally {
            mvStore.deregisterVersionUsage(txCounter);
        }
    }

    @Override
    public RecordStream<Key> keys() {
        return RecordStream.fromIterable(mvMap.keySet());
    }

    @Override
    public void put(Key key, Value value) {
        notNull(value, "value cannot be null");
        MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
        try {
            mvMap.put(key, value);
            updateLastModifiedTime();
        } finally {
            mvStore.deregisterVersionUsage(txCounter);
        }
    }

    @Override
    public long size() {
        return mvMap.sizeAsLong();
    }

    @Override
    public Value putIfAbsent(Key key, Value value) {
        notNull(value, "value cannot be null");
        MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
        try {
            Value v = mvMap.putIfAbsent(key, value);
            updateLastModifiedTime();
            return v;
        } finally {
            mvStore.deregisterVersionUsage(txCounter);
        }
    }

    @Override
    public RecordStream<Pair<Key, Value>> entries() {
        return EntryIterator::new;
    }

    /**
     * The map's entries, with a skip that descends the tree by index instead of walking it.
     *
     * <p>An MVStore page records how many entries sit beneath it, so {@link MVMap#getKey(long)} is
     * a O(log n) descent rather than a scan - the same property that makes {@code keyList()} a
     * random-access list. Paging a collection therefore costs its own page rather than every page
     * before it, which is the difference between a linear and a flat page latency.
     */
    private class EntryIterator implements Iterator<Pair<Key, Value>>, SkippableIterator {
        private Iterator<Map.Entry<Key, Value>> delegate = mvMap.entrySet().iterator();

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Pair<Key, Value> next() {
            Map.Entry<Key, Value> entry = delegate.next();
            return new Pair<>(entry.getKey(), entry.getValue());
        }

        @Override
        public long skip(long count) {
            long size = mvMap.sizeAsLong();
            // Read once, and re-checked below: the map is live, so a concurrent removal between
            // the size and the seek would otherwise be a null key rather than a short skip.
            if (count >= size) {
                delegate = Collections.emptyIterator();
                return size;
            }

            Key from = mvMap.getKey(count);
            if (from == null) {
                delegate = Collections.emptyIterator();
                return size;
            }

            // cursor(from) positions at the first key >= from, which is the key at index `count`
            // - the first one the caller actually wants.
            Cursor<Key, Value> cursor = mvMap.cursor(from);
            delegate = new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return cursor.hasNext();
                }

                @Override
                public Map.Entry<Key, Value> next() {
                    Key key = cursor.next();
                    // getValue() belongs to the entry next() just returned; it is read from the
                    // page the cursor is already sitting on.
                    return new AbstractMap.SimpleImmutableEntry<>(key, cursor.getValue());
                }
            };
            return count;
        }
    }

    @Override
    public RecordStream<Pair<Key, Value>> reversedEntries() {
        return () -> new ReverseIterator<>(mvMap);
    }

    @Override
    public Key firstKey() {
        return mvMap.firstKey();
    }

    @Override
    public Key lastKey() {
        return mvMap.lastKey();
    }

    @Override
    public Key higherKey(Key key) {
        return mvMap.higherKey(key);
    }

    @Override
    public Key ceilingKey(Key key) {
        return mvMap.ceilingKey(key);
    }

    @Override
    public Key lowerKey(Key key) {
        return mvMap.lowerKey(key);
    }

    @Override
    public Key floorKey(Key key) {
        return mvMap.floorKey(key);
    }

    @Override
    public boolean isEmpty() {
        return mvMap.isEmpty();
    }

    @Override
    public void drop() {
        if (!droppedFlag.get()) {
            droppedFlag.compareAndSet(false, true);
            closedFlag.compareAndSet(false, true);

            MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
            try {
                nitriteStore.closeMap(getName());
                nitriteStore.removeMap(getName());
            } finally {
                mvStore.deregisterVersionUsage(txCounter);
            }
        }
    }

    @Override
    public boolean isDropped() {
        return droppedFlag.get();
    }

    @Override
    public void close() {
        if (!closedFlag.get() && !droppedFlag.get()) {
            closedFlag.compareAndSet(false, true);
            nitriteStore.closeMap(getName());
        }
    }

    @Override
    public boolean isClosed() {
        return closedFlag.get();
    }
}
