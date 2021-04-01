package org.dizitart.no2.transaction;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.NitriteRTree;

import java.util.*;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
class TransactionalRTree<Key extends BoundingBox, Value> implements NitriteRTree<Key, Value> {
    private final Map<SpatialKey, Key> map;
    private final NitriteRTree<Key, Value> primary;

    public TransactionalRTree(NitriteRTree<Key, Value> primary) {
        this.map = new HashMap<>();
        this.primary = primary;
    }

    @Override
    public void add(Key key, NitriteId nitriteId) {
        if (nitriteId != null && nitriteId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(nitriteId.getIdValue()));
            map.put(spatialKey, key);
        }
    }

    @Override
    public void remove(Key key, NitriteId nitriteId) {
        if (nitriteId != null && nitriteId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(nitriteId.getIdValue()));
            map.remove(spatialKey);
        }
    }

    @Override
    public RecordStream<NitriteId> findIntersectingKeys(Key key) {
        SpatialKey spatialKey = getKey(key, 0L);
        Set<NitriteId> set = new HashSet<>();

        for (SpatialKey sk : map.keySet()) {
            if (isOverlap(sk, spatialKey)) {
                set.add(NitriteId.createId(Long.toString(sk.getId())));
            }
        }

        RecordStream<NitriteId> primaryRecords = primary.findIntersectingKeys(key);
        return RecordStream.fromCombined(primaryRecords, set);
    }

    @Override
    public RecordStream<NitriteId> findContainedKeys(Key key) {
        SpatialKey spatialKey = getKey(key, 0L);
        Set<NitriteId> set = new HashSet<>();

        for (SpatialKey sk : map.keySet()) {
            if (isInside(sk, spatialKey)) {
                set.add(NitriteId.createId(Long.toString(sk.getId())));
            }
        }

        RecordStream<NitriteId> primaryRecords = primary.findContainedKeys(key);
        return RecordStream.fromCombined(primaryRecords, set);
    }

    @Override
    public long size() {
        return map.size();
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

    private SpatialKey getKey(Key key, long id) {
        return new SpatialKey(id, key.getMinX(),
            key.getMaxX(), key.getMinY(), key.getMaxY());
    }

    @Override
    public void close() throws Exception {
        map.clear();
    }

    /*
     * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
     * and the EPL 1.0 (https://h2database.com/html/license.html).
     * Initial Developer: H2 Group
     */
    private static class SpatialKey {

        private final long id;
        private final float[] minMax;

        public SpatialKey(long id, float... minMax) {
            this.id = id;
            this.minMax = minMax;
        }

        public float min(int dim) {
            return minMax[dim + dim];
        }

        public void setMin(int dim, float x) {
            minMax[dim + dim] = x;
        }

        public float max(int dim) {
            return minMax[dim + dim + 1];
        }

        public void setMax(int dim, float x) {
            minMax[dim + dim + 1] = x;
        }

        public long getId() {
            return id;
        }

        public boolean isNull() {
            return minMax.length == 0;
        }

        @Override
        public int hashCode() {
            return (int) ((id >>> 32) ^ id);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else if (!(other instanceof SpatialKey)) {
                return false;
            }
            SpatialKey o = (SpatialKey) other;
            if (id != o.id) {
                return false;
            }
            return equalsIgnoringId(o);
        }

        public boolean equalsIgnoringId(SpatialKey o) {
            return Arrays.equals(minMax, o.minMax);
        }
    }
}
