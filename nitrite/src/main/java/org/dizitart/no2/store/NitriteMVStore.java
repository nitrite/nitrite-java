/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.store;


import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;

import java.util.HashSet;
import java.util.Set;

import static org.dizitart.no2.common.Constants.COLLECTION_CATALOG;

/**
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Slf4j
public class NitriteMVStore extends AbstractNitriteStore {
    private MVStore mvStore;
    private MVStoreConfig mvStoreConfig;

    public NitriteMVStore() {
        super();
    }

    @Override
    public void openOrCreate(String username, String password, StoreConfig storeConfig) {
        validateStoreConfig(storeConfig);
        this.mvStoreConfig = (MVStoreConfig) storeConfig;
        this.mvStore = MVStoreUtils.openOrCreate(username, password, mvStoreConfig);
        initEventBus();
        alert(StoreEvents.Opened);
    }

    @Override
    public boolean isClosed() {
        return mvStore.isClosed();
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
    public void close() {
        if (mvStoreConfig.isAutoCompact()) {
            compact();
        }
        mvStore.close();
        alert(StoreEvents.Closed);
    }

    @Override
    public <Key, Value> NitriteMap<Key, Value> openMap(String name) {
        MVMap<Key, Value> mvMap = mvStore.openMap(name);
        return new NitriteMVMap<>(mvMap, this);
    }

    @Override
    public void removeMap(String name) {
        MVMap<?, ?> mvMap = mvStore.openMap(name);
        mvStore.removeMap(mvMap);

        NitriteMap<String, Document> catalogueMap = openMap(COLLECTION_CATALOG);
        for (KeyValuePair<String, Document> entry : catalogueMap.entries()) {
            String catalogue = entry.getKey();
            Document document = entry.getValue();

            Set<String> bin = new HashSet<>();
            boolean foundKey = false;
            for (String field : document.getFields()) {
                if (field.equals(name)) {
                    foundKey = true;
                    bin.add(field);
                }
            }

            for (String field : bin) {
                document.remove(field);
            }
            catalogueMap.put(catalogue, document);

            if (foundKey) break;
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String name) {
        MVRTreeMap<Value> map = mvStore.openMap(name, new MVRTreeMap.Builder<>());
        return new NitriteMVRTreeMap(map);
    }

    @Override
    public StoreInfo getStoreInfo() {
        return MVStoreUtils.getStoreInfo(mvStore);
    }

    public void compact() {
        mvStore.compactMoveChunks();
    }

    private void validateStoreConfig(StoreConfig storeConfig) {
        if (!(storeConfig instanceof MVStoreConfig)) {
            throw new ValidationException("store config is not valid mv store config");
        }
    }

    private void initEventBus() {
        if (mvStoreConfig.getEventListeners() != null) {
            for (StoreEventListener eventListener : mvStoreConfig.getEventListeners()) {
                eventBus.register(eventListener);
            }
        }
    }
}
