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
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.AbstractNitriteStore;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreException;
import org.h2.mvstore.rtree.MVRTreeMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.h2.mvstore.DataUtils.*;

/**
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Slf4j
public class NitriteMVStore extends AbstractNitriteStore<MVStoreConfig> {
    private MVStore mvStore;
    private final Map<String, NitriteMap<?, ?>> nitriteMapRegistry;
    private final Map<String, NitriteRTree<?, ?>> nitriteRTreeMapRegistry;
    private volatile boolean autoCommitPending;

    public NitriteMVStore() {
        super();
        this.nitriteMapRegistry = new ConcurrentHashMap<>();
        this.nitriteRTreeMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void openOrCreate() {
        // MVStoreUtils always opens with auto-commit disabled; it is enabled lazily
        // right after the very first map is created (see enableAutoCommitIfPending()),
        // so the background writer never races with bootstrapping a brand new store
        this.autoCommitPending = getStoreConfig().autoCommit();
        this.mvStore = MVStoreUtils.openOrCreate(getStoreConfig());
        initEventBus();
        alert(StoreEvents.Opened);
    }

    private void enableAutoCommitIfPending() {
        if (autoCommitPending) {
            autoCommitPending = false;
            mvStore.commit();
            mvStore.setAutoCommitDelay(1000);
        }
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
        // pause the background writer for the duration of this explicit commit;
        // H2's autocommit thread and an explicit commit() racing on the same store
        // can trip an internal MVStore assertion (concurrent chunk serialization)
        int delay = mvStore.getAutoCommitDelay();
        if (delay > 0) {
            mvStore.setAutoCommitDelay(0);
        }
        try {
            mvStore.commit();
        } finally {
            if (delay > 0) {
                mvStore.setAutoCommitDelay(delay);
            }
        }
        alert(StoreEvents.Commit);
    }

    @Override
    public void close() {
        // close nitrite maps
        for (NitriteMap<?, ?> nitriteMap : nitriteMapRegistry.values()) {
            nitriteMap.close();
        }

        for (NitriteRTree<?, ?> rTree : nitriteRTreeMapRegistry.values()) {
            rTree.close();
        }

        nitriteMapRegistry.clear();
        nitriteRTreeMapRegistry.clear();

        if (getStoreConfig().autoCompact()) {
           mvStore.close(-1);
        } else {
            mvStore.close();
        }
        alert(StoreEvents.Closed);
        eventBus.close();
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

        MVMap<Key, Value> mvMap = openMVMap(mapName, null);
        NitriteMVMap<Key, Value> nitriteMVMap = new NitriteMVMap<>(mvMap, this);
        nitriteMapRegistry.put(mapName, nitriteMVMap);
        return nitriteMVMap;
    }

    @Override
    public void closeMap(String mapName) {
        if (!StringUtils.isNullOrEmpty(mapName)) {
            nitriteMapRegistry.remove(mapName);
        }
    }

    @Override
    public void closeRTree(String rTreeName) {
        if (!StringUtils.isNullOrEmpty(rTreeName)) {
            nitriteRTreeMapRegistry.remove(rTreeName);
        }
    }

    @Override
    public void removeMap(String name) {
        MVMap<?, ?> mvMap = openMVMap(name, null);
        mvStore.removeMap(mvMap);
        getCatalog().remove(name);
        nitriteMapRegistry.remove(name);
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public void removeRTree(String rTreeName) {
        MVMap mvMap = openMVMap(rTreeName, new MVRTreeMap.Builder<>());
        mvStore.removeMap(mvMap);
        getCatalog().remove(rTreeName);
        nitriteRTreeMapRegistry.remove(rTreeName);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String mapName, Class<?> keyType, Class<?> valueType) {
        if (nitriteRTreeMapRegistry.containsKey(mapName)) {
            return (NitriteMVRTreeMap) nitriteRTreeMapRegistry.get(mapName);
        }

        MVRTreeMap<Value> map = (MVRTreeMap<Value>) openMVMap(mapName, new MVRTreeMap.Builder<>());
        NitriteMVRTreeMap<Key, Value> nitriteMVRTreeMap = new NitriteMVRTreeMap(map, this);
        nitriteRTreeMapRegistry.put(mapName, nitriteMVRTreeMap);
        return nitriteMVRTreeMap;
    }

    @Override
    public String getStoreVersion() {
        return "MVStore/" + org.h2.engine.Constants.VERSION;
    }

    private void initEventBus() {
        if (getStoreConfig().eventListeners() != null) {
            for (StoreEventListener eventListener : getStoreConfig().eventListeners()) {
                eventBus.register(eventListener);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private MVMap openMVMap(String mapName, MVMap.MapBuilder builder) {
        Exception exception = null;
        try {
            MVMap.MapBuilder mapBuilder = builder == null ? new MVMap.Builder<>() : builder;
            long version = mvStore.getCurrentVersion();

            while (version >= 0) {
                try {
                    MVMap map = mvStore.openMap(mapName, mapBuilder);
                    enableAutoCommitIfPending();
                    return map;
                } catch (MVStoreException me) {
                    if (version == 0) {
                        throw me;
                    }

                    log.warn("Error opening map {} with version {}, retrying with previous version", mapName, version, me);
                    if (me.getErrorCode() == ERROR_READING_FAILED || me.getErrorCode() == ERROR_WRITING_FAILED
                        || me.getErrorCode() == ERROR_FILE_CORRUPT || me.getErrorCode() == ERROR_SERIALIZATION
                        || me.getErrorCode() == ERROR_CHUNK_NOT_FOUND || me.getErrorCode() == ERROR_BLOCK_NOT_FOUND) {
                        // open map with earlier version
                        mvStore.rollbackTo(version - 1);
                        version = mvStore.getCurrentVersion();
                    } else {
                        throw me;
                    }
                    exception = me;
                }
            }
        } catch (Exception e) {
            exception = e;
        }

        throw new NitriteIOException("Unable to open map " + mapName, exception);
    }
}
