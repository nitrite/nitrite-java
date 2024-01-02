package org.dizitart.no2.store.memory;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.util.SpatialKey;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.NitriteStore;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class InMemoryRTree<Key extends BoundingBox, Value> implements NitriteRTree<Key, Value> {
    private final Map<SpatialKey, Key> backingMap;
    private final AtomicBoolean droppedFlag;
    private final AtomicBoolean closedFlag;
    private final String mapName;
    private final NitriteStore<?> nitriteStore;

    /**
     * Instantiates a new {@link InMemoryRTree}.
     */
    public InMemoryRTree(String mapName, NitriteStore<?> nitriteStore) {
        this.backingMap = new ConcurrentHashMap<>();
        this.closedFlag = new AtomicBoolean(false);
        this.droppedFlag = new AtomicBoolean(false);
        this.mapName = mapName;
        this.nitriteStore = nitriteStore;
    }

    @Override
    public void add(Key key, NitriteId nitriteId) {
        checkOpened();
        if (nitriteId != null && nitriteId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(nitriteId.getIdValue()));
            backingMap.put(spatialKey, key);
        }
    }

    @Override
    public void remove(Key key, NitriteId nitriteId) {
        checkOpened();
        if (nitriteId != null && nitriteId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(nitriteId.getIdValue()));
            backingMap.remove(spatialKey);
        }
    }

    @Override
    public RecordStream<NitriteId> findIntersectingKeys(Key key) {
        checkOpened();
        SpatialKey spatialKey = getKey(key, 0L);
        Set<NitriteId> set = new HashSet<>();

        for (SpatialKey sk : backingMap.keySet()) {
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

        for (SpatialKey sk : backingMap.keySet()) {
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
        nitriteStore.removeRTree(mapName);
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
