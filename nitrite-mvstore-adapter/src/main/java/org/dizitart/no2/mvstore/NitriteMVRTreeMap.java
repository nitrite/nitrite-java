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

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.NitriteStore;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;

import java.util.Iterator;

/**
 * @since 1.0
 * @author Anindya Chatterjee
 */
class NitriteMVRTreeMap<Key extends BoundingBox, Value> implements NitriteRTree<Key, Value> {
    private final MVRTreeMap<Key> mvMap;
    private final NitriteStore<?> nitriteStore;

    NitriteMVRTreeMap(MVRTreeMap<Key> mvMap, NitriteStore<?> nitriteStore) {
        this.mvMap = mvMap;
        this.nitriteStore = nitriteStore;
    }

    @Override
    public void add(Key key, NitriteId nitriteId) {
        if (nitriteId != null && nitriteId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(nitriteId.getIdValue()));
            MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
            try {
                mvMap.add(spatialKey, key);
            } finally {
                mvStore.deregisterVersionUsage(txCounter);
            }
        }
    }

    @Override
    public void remove(Key key, NitriteId nitriteId) {
        if (nitriteId != null && nitriteId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(nitriteId.getIdValue()));
            MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
            try {
                mvMap.remove(spatialKey);
            } finally {
                mvStore.deregisterVersionUsage(txCounter);
            }
        }
    }

    @Override
    public RecordStream<NitriteId> findIntersectingKeys(Key key) {
        SpatialKey spatialKey = getKey(key, 0L);
        MVRTreeMap.RTreeCursor treeCursor = mvMap.findIntersectingKeys(spatialKey);
        return getRecordStream(treeCursor);
    }

    @Override
    public RecordStream<NitriteId> findContainedKeys(Key key) {
        SpatialKey spatialKey = getKey(key, 0L);
        MVRTreeMap.RTreeCursor treeCursor = mvMap.findContainedKeys(spatialKey);
        return getRecordStream(treeCursor);
    }

    @Override
    public long size() {
        return mvMap.sizeAsLong();
    }

    private SpatialKey getKey(Key key, long id) {
        if (key == null) {
            return new SpatialKey(id);
        } else {
            return new SpatialKey(id, key.getMinX(),
                key.getMaxX(), key.getMinY(), key.getMaxY());
        }
    }

    private RecordStream<NitriteId> getRecordStream(MVRTreeMap.RTreeCursor treeCursor) {
        return RecordStream.fromIterable(() -> new Iterator<NitriteId>() {
            @Override
            public boolean hasNext() {
                return treeCursor.hasNext();
            }

            @Override
            public NitriteId next() {
                SpatialKey next = treeCursor.next();
                return NitriteId.createId(Long.toString(next.getId()));
            }
        });
    }

    @Override
    public void close() {
        nitriteStore.closeRTree(mvMap.getName());
    }

    @Override
    public void clear() {
        mvMap.clear();
    }

    @Override
    public void drop() {
        mvMap.clear();
        nitriteStore.closeRTree(mvMap.getName());
        nitriteStore.removeRTree(mvMap.getName());
    }
}
