/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.store;

import org.dizitart.no2.meta.Attributes;
import org.h2.mvstore.MVMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.META_MAP_NAME;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

/**
 * A {@link MVMap} backed {@link NitriteMap} implementation.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
class NitriteMVMap<Key, Value> implements NitriteMap<Key, Value> {
    private MVMap<Key, Value> mvMap;
    private NitriteStore nitriteStore;

    NitriteMVMap(MVMap<Key, Value> mvMap, NitriteStore nitriteStore) {
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
    public NitriteStore getStore() {
        return nitriteStore;
    }

    @Override
    public void clear() {
        updateAttributes();
        mvMap.clear();
    }

    @Override
    public String getName() {
        return mvMap.getName();
    }

    @Override
    public Collection<Value> values() {
        return mvMap.values();
    }

    @Override
    public Value remove(Key key) {
        updateAttributes();
        return mvMap.remove(key);
    }

    @Override
    public Set<Key> keySet() {
        return mvMap.keySet();
    }

    @Override
    public void put(Key key, Value value) {
        updateAttributes();
        mvMap.put(key, value);
    }

    @Override
    public int size() {
        return mvMap.size();
    }

    @Override
    public long sizeAsLong() {
        return mvMap.sizeAsLong();
    }

    @Override
    public Value putIfAbsent(Key nitriteId, Value document) {
        updateAttributes();
        return mvMap.putIfAbsent(nitriteId, document);
    }

    @Override
    public Set<Map.Entry<Key, Value>> entrySet() {
        return mvMap.entrySet();
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
    public List<Key> keyList() {
        return mvMap.keyList();
    }

    @Override
    public void drop() {
        nitriteStore.removeMap(this);
    }

    @Override
    public Attributes getAttributes() {
        NitriteMap<String, Attributes> metaMap = nitriteStore.metaMap();
        if (metaMap != null && !getName().contentEquals(META_MAP_NAME)) {
            return metaMap.get(getName());
        }
        return null;
    }

    @Override
    public void setAttributes(Attributes attributes) {
        NitriteMap<String, Attributes> metaMap = nitriteStore.metaMap();
        if (metaMap != null && !getName().contentEquals(META_MAP_NAME)) {
            metaMap.put(getName(), attributes);
        }
    }

    MVMap<Key, Value> getUnderlyingMVMap() {
        return mvMap;
    }

    private void updateAttributes() {
        if (isNullOrEmpty(getName())
                || META_MAP_NAME.equals(getName())) return;

        NitriteMap<String, Attributes> metaMap = nitriteStore.metaMap();
        if (metaMap != null) {
            Attributes attributes = metaMap.get(getName());
            if (attributes == null) {
                attributes = new Attributes(getName());
                metaMap.put(getName(), attributes);
            }
            attributes.setLastModifiedTime(System.currentTimeMillis());
        }
    }
}
