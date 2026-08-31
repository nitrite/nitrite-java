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

import java.lang.ref.Cleaner;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.NitriteStore;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;

/**
 * @author Anindya Chatterjee
 * @since 1.0
 */
class NitriteMVRTreeMap<Key extends BoundingBox, Value> implements NitriteRTree<Key, Value> {

    private final MVRTreeMap<Key> mvMap;
    private final NitriteStore<?> nitriteStore;
    private final MVStore mvStore;
    private final Set<VersionUsage> versionUsages;

    NitriteMVRTreeMap(final MVRTreeMap<Key> mvMap, final NitriteStore<?> nitriteStore) {
        this.mvMap = mvMap;
        this.nitriteStore = nitriteStore;
        this.mvStore = mvMap.getStore();
        this.versionUsages = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void add(final Key key, final NitriteId nitriteId) {
        if (nitriteId != null) {
            final MVSpatialKey spatialKey = getKey(key, nitriteId.getIdValue());
            final MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
            try {
                mvMap.add(spatialKey, key);
            } finally {
                mvStore.deregisterVersionUsage(txCounter);
            }
        }
    }

    @Override
    public void remove(final Key key, final NitriteId nitriteId) {
        if (nitriteId != null) {
            final MVSpatialKey spatialKey = getKey(key, nitriteId.getIdValue());
            final MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
            try {
                mvMap.remove(spatialKey);
            } finally {
                mvStore.deregisterVersionUsage(txCounter);
            }
        }
    }

    @Override
    public RecordStream<NitriteId> findIntersectingKeys(final Key key) {
        final MVSpatialKey spatialKey = getKey(key, 0L);
        return getRecordStream(() -> mvMap.findIntersectingKeys(spatialKey));
    }

    @Override
    public RecordStream<NitriteId> findContainedKeys(final Key key) {
        final MVSpatialKey spatialKey = getKey(key, 0L);
        return getRecordStream(() -> mvMap.findContainedKeys(spatialKey));
    }

    @Override
    public long size() {
        return mvMap.sizeAsLong();
    }

    private MVSpatialKey getKey(final Key key, final long id) {
        if (key == null || key.equals(BoundingBox.EMPTY)) {
            return new MVSpatialKey(id);
        } else {
            return new MVSpatialKey(id, key.getMinX(),
                key.getMaxX(), key.getMinY(), key.getMaxY());
        }
    }

    private RecordStream<NitriteId> getRecordStream(
        final Supplier<MVRTreeMap.RTreeCursor<Key>> cursorSupplier) {
        return RecordStream.fromIterable(() -> new VersionedCursor(cursorSupplier));
    }

    @Override
    public void close() {
        releaseVersionUsages();
        nitriteStore.closeRTree(mvMap.getName());
    }

    @Override
    public void clear() {
        mvMap.clear();
    }

    @Override
    public void drop() {
        releaseVersionUsages();
        mvMap.clear();
        nitriteStore.closeRTree(mvMap.getName());
        nitriteStore.removeRTree(mvMap.getName());
    }

    private void releaseVersionUsages() {
        for (final VersionUsage versionUsage : versionUsages) {
            versionUsage.release();
        }
    }

    private class VersionedCursor implements Iterator<NitriteId> {

        private final MVRTreeMap.RTreeCursor<Key> treeCursor;
        private final Cleaner.Cleanable cleanable;
        private final VersionUsage versionUsage;
        private boolean exhausted;

        private VersionedCursor(final Supplier<MVRTreeMap.RTreeCursor<Key>> cursorSupplier) {

            versionUsage = new VersionUsage(mvStore, mvStore.registerVersionUsage(), versionUsages);
            versionUsages.add(versionUsage);

            try {
                treeCursor = cursorSupplier.get();
                cleanable = VersionUsage.CLEANER.register(this, versionUsage::release);
            } catch (final RuntimeException | Error e) {
                versionUsage.release();
                throw e;
            }
        }

        @Override
        public boolean hasNext() {
            if (exhausted) {
                return false;
            }
            ensureOpen();
            try {
                final boolean hasNext = treeCursor.hasNext();
                if (!hasNext) {
                    exhausted = true;
                    cleanable.clean();
                }
                return hasNext;
            } catch (final RuntimeException | Error e) {
                cleanable.clean();
                throw e;
            }
        }

        @Override
        public NitriteId next() {
            if (exhausted) {
                throw new NoSuchElementException();
            }
            ensureOpen();
            try {
                final MVSpatialKey next = (MVSpatialKey) treeCursor.next();
                if (next == null) {
                    exhausted = true;
                    cleanable.clean();
                    throw new NoSuchElementException();
                }
                return NitriteId.createId(Long.toString(next.getId()));
            } catch (final RuntimeException | Error e) {
                cleanable.clean();
                throw e;
            }
        }

        private void ensureOpen() {
            if (versionUsage.isReleased()) {
                throw new NitriteIOException("MVStore is closed");
            }
        }
    }
}
