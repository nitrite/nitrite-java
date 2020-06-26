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

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.index.BoundingBox;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;

import java.util.Iterator;

/**
 * @since 1.0
 * @author Anindya Chatterjee
 */
class NitriteMVRTreeMap<Key extends BoundingBox, Value>
    implements NitriteRTree<Key, Value> {
    private final MVRTreeMap<Key> mvMap;

    NitriteMVRTreeMap(MVRTreeMap<Key> mvMap) {
        this.mvMap = mvMap;
    }

    @Override
    public void add(Key key, NitriteId nitriteId) {
        if (nitriteId != null && nitriteId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(nitriteId.getIdValue()));
            mvMap.add(spatialKey, key);
        }
    }

    @Override
    public void remove(Key key, NitriteId nitriteId) {
        if (nitriteId != null && nitriteId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(nitriteId.getIdValue()));
            mvMap.remove(spatialKey);
        }
    }

    @Override
    public ReadableStream<NitriteId> findIntersectingKeys(Key key) {
        SpatialKey spatialKey = getKey(key, 0L);
        MVRTreeMap.RTreeCursor treeCursor = mvMap.findIntersectingKeys(spatialKey);
        return ReadableStream.fromIterator(new Iterator<NitriteId>() {
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
    public ReadableStream<NitriteId> findContainedKeys(Key key) {
        SpatialKey spatialKey = getKey(key, 0L);
        MVRTreeMap.RTreeCursor treeCursor = mvMap.findContainedKeys(spatialKey);
        return ReadableStream.fromIterator(new Iterator<NitriteId>() {
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

    private SpatialKey getKey(Key key, long id) {
        return new SpatialKey(id, key.getMinX(),
            key.getMaxX(), key.getMinY(), key.getMaxY());
    }
}
