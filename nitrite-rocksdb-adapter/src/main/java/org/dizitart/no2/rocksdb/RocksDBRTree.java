/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.rocksdb;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.util.SpatialKey;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.NitriteRTree;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
public class RocksDBRTree<Key extends BoundingBox, Value> implements NitriteRTree<Key, Value> {
    private final RocksDBMap<SpatialKey, Key> backingMap;
    private final AtomicBoolean droppedFlag;
    private final AtomicBoolean closedFlag;

    public RocksDBRTree(RocksDBMap<SpatialKey, Key> backingMap) {
        this.backingMap = backingMap;
        this.closedFlag = new AtomicBoolean(false);
        this.droppedFlag = new AtomicBoolean(false);
    }

    @Override
    public void add(Key key, NitriteId nitriteId) {
        checkOpened();
        if (nitriteId != null) {
            SpatialKey spatialKey = getKey(key, nitriteId.getIdValue());
            backingMap.put(spatialKey, key);
        }
    }

    @Override
    public void remove(Key key, NitriteId nitriteId) {
        checkOpened();
        if (nitriteId != null) {
            SpatialKey spatialKey = getKey(key, nitriteId.getIdValue());
            backingMap.remove(spatialKey);
        }
    }

    @Override
    public RecordStream<NitriteId> findIntersectingKeys(Key key) {
        checkOpened();
        SpatialKey spatialKey = getKey(key, 0L);
        Set<NitriteId> set = new HashSet<>();

        for (SpatialKey sk : backingMap.keys()) {
            if (isOverlap(sk, spatialKey)) {
                set.add(NitriteId.createId(Long.toString(sk.getId())));
            }
        }

        return RecordStream.fromIterable(set);
    }

    @Override
    public RecordStream<NitriteId> findContainedKeys(Key key) {
        checkOpened();
        SpatialKey spatialKey = getKey(key, 0L);
        Set<NitriteId> set = new HashSet<>();

        for (SpatialKey sk : backingMap.keys()) {
            if (isInside(sk, spatialKey)) {
                set.add(NitriteId.createId(Long.toString(sk.getId())));
            }
        }

        return RecordStream.fromIterable(set);
    }

    @Override
    public long size() {
        checkOpened();
        return backingMap.size();
    }

    @Override
    public void close() {
        closedFlag.compareAndSet(false, true);
    }

    @Override
    public void clear() {
        checkOpened();
        backingMap.clear();
    }

    @Override
    public void drop() {
        checkOpened();
        droppedFlag.compareAndSet(false, true);
        backingMap.clear();
    }

    private SpatialKey getKey(Key key, long id) {
        if (key == null || key.equals(BoundingBox.EMPTY)) {
            return new SpatialKey(id);
        }
        return new SpatialKey(id, key.getMinX(),
            key.getMaxX(), key.getMinY(), key.getMaxY());
    }

    private boolean isOverlap(SpatialKey a, SpatialKey b) {
        if (a.isNull() || b.isNull()) {
            return false;
        }
        for (int i = 0; i < 2; i++) {
            if (a.max(i) < b.min(i) || a.min(i) > b.max(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInside(SpatialKey a, SpatialKey b) {
        if (a.isNull() || b.isNull()) {
            return false;
        }
        for (int i = 0; i < 2; i++) {
            if (a.min(i) <= b.min(i) || a.max(i) >= b.max(i)) {
                return false;
            }
        }
        return true;
    }

    private void checkOpened() {
        if (closedFlag.get()) {
            throw new InvalidOperationException("RTreeMap is closed");
        }

        if (droppedFlag.get()) {
            throw new InvalidOperationException("RTreeMap is dropped");
        }
    }
}
