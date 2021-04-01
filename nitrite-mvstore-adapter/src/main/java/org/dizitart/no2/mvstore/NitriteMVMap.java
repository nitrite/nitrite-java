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

import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.h2.mvstore.MVMap;

import java.util.Iterator;
import java.util.Map;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @since 1.0
 * @author Anindya Chatterjee
 */
class NitriteMVMap<Key, Value> implements NitriteMap<Key, Value> {
    private final MVMap<Key, Value> mvMap;
    private final NitriteStore<?> nitriteStore;

    NitriteMVMap(MVMap<Key, Value> mvMap, NitriteStore<?> nitriteStore) {
        this.mvMap = mvMap;
        this.nitriteStore = nitriteStore;
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
        mvMap.clear();
        updateLastModifiedTime();
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
        Value value = mvMap.remove(key);
        updateLastModifiedTime();
        return value;
    }

    @Override
    public RecordStream<Key> keys() {
        return RecordStream.fromIterable(mvMap.keySet());
    }

    @Override
    public void put(Key key, Value value) {
        notNull(value, "value cannot be null");
        mvMap.put(key, value);
        updateLastModifiedTime();
    }

    @Override
    public long size() {
        return mvMap.sizeAsLong();
    }

    @Override
    public Value putIfAbsent(Key key, Value value) {
        notNull(value, "value cannot be null");
        Value v = mvMap.putIfAbsent(key, value);
        updateLastModifiedTime();
        return v;
    }

    @Override
    public RecordStream<Pair<Key, Value>> entries() {
        return () -> new Iterator<Pair<Key, Value>>() {
            final Iterator<Map.Entry<Key, Value>> entryIterator = mvMap.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public Pair<Key, Value> next() {
                Map.Entry<Key, Value> entry = entryIterator.next();
                return new Pair<>(entry.getKey(), entry.getValue());
            }
        };
    }

    @Override
    public RecordStream<Pair<Key, Value>> reversedEntries() {
        return () -> new ReverseIterator<>(mvMap);
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
        nitriteStore.removeMap(getName());
    }

    @Override
    public void close() {
        // nothing to close
    }
}
