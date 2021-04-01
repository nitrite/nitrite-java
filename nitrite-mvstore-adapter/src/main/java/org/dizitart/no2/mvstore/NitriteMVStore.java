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


import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.AbstractNitriteStore;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Slf4j
public class NitriteMVStore extends AbstractNitriteStore<MVStoreConfig> {
    private MVStore mvStore;
    private final Map<String, NitriteMap<?, ?>> nitriteMapRegistry;
    private final Map<String, NitriteRTree<?, ?>> nitriteRTreeMapRegistry;

    public NitriteMVStore() {
        super();
        this.nitriteMapRegistry = new ConcurrentHashMap<>();
        this.nitriteRTreeMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void openOrCreate() {
        this.mvStore = MVStoreUtils.openOrCreate(getStoreConfig());
        initEventBus();
        alert(StoreEvents.Opened);
    }

    @Override
    public boolean isClosed() {
        return mvStore == null || mvStore.isClosed();
    }

    @Override
    public boolean hasUnsavedChanges() {
        return mvStore != null && mvStore.hasUnsavedChanges();
    }

    @Override
    public boolean isReadOnly() {
        return mvStore.isReadOnly();
    }

    @Override
    public void commit() {
        mvStore.commit();
        alert(StoreEvents.Commit);
    }

    @Override
    public void close() throws Exception {
        if (getStoreConfig().autoCompact()) {
            compact();
        }

        // close nitrite maps
        for (NitriteMap<?, ?> nitriteMap : nitriteMapRegistry.values()) {
            nitriteMap.close();
        }

        for (NitriteRTree<?, ?> rTree : nitriteRTreeMapRegistry.values()) {
            rTree.close();
        }

        mvStore.close();
        alert(StoreEvents.Closed);
    }

    @Override
    public boolean hasMap(String mapName) {
        return mvStore.hasMap(mapName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Key, Value> NitriteMap<Key, Value> openMap(String mapName, Class<?> keyType, Class<?> valueType) {
        if (nitriteMapRegistry.containsKey(mapName)) {
            return (NitriteMVMap<Key, Value>) nitriteMapRegistry.get(mapName);
        }

        MVMap<Key, Value> mvMap = mvStore.openMap(mapName);
        NitriteMVMap<Key, Value> nitriteMVMap = new NitriteMVMap<>(mvMap, this);
        nitriteMapRegistry.put(mapName, nitriteMVMap);
        return nitriteMVMap;
    }

    @Override
    public void removeMap(String name) {
        MVMap<?, ?> mvMap = mvStore.openMap(name);
        mvStore.removeMap(mvMap);
        nitriteMapRegistry.remove(name);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String mapName, Class<?> keyType, Class<?> valueType) {
        if (nitriteRTreeMapRegistry.containsKey(mapName)) {
            return (NitriteMVRTreeMap) nitriteRTreeMapRegistry.get(mapName);
        }

        MVRTreeMap<Value> map = mvStore.openMap(mapName, new MVRTreeMap.Builder<>());
        NitriteMVRTreeMap<Key, Value> nitriteMVRTreeMap = new NitriteMVRTreeMap(map);
        nitriteRTreeMapRegistry.put(mapName, nitriteMVRTreeMap);
        return nitriteMVRTreeMap;
    }

    @Override
    public String getStoreVersion() {
        return "MVStore/" + org.h2.engine.Constants.VERSION;
    }

    public void compact() {
        mvStore.compactMoveChunks();
    }

    private void initEventBus() {
        if (getStoreConfig().eventListeners() != null) {
            for (StoreEventListener eventListener : getStoreConfig().eventListeners()) {
                eventBus.register(eventListener);
            }
        }
    }
}
