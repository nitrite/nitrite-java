package org.dizitart.no2.transaction;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.util.SpatialKey;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.NitriteRTree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        if (nitriteId != null) {
            SpatialKey spatialKey = getKey(key, nitriteId.getIdValue());
            map.put(spatialKey, key);
        }
    }

    @Override
    public void remove(Key key, NitriteId nitriteId) {
        if (nitriteId != null) {
            SpatialKey spatialKey = getKey(key, nitriteId.getIdValue());
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
        if (key == null || key.equals(BoundingBox.EMPTY)) {
            return new SpatialKey(id);
        }
        return new SpatialKey(id, key.getMinX(),
            key.getMaxX(), key.getMinY(), key.getMaxY());
    }

    @Override
    public void close() {
        map.clear();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public void drop() {
        map.clear();
    }
}
