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

import static org.dizitart.no2.common.util.ValidationUtils.notNull;

import java.lang.ref.Cleaner;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

/**
 * @author Anindya Chatterjee
 * @since 1.0
 */
class NitriteMVMap<Key, Value> implements NitriteMap<Key, Value> {

    private static final Cleaner CLEANER = Cleaner.create();

    private final MVMap<Key, Value> mvMap;
    private final NitriteStore<?> nitriteStore;
    private final MVStore mvStore;
    private final AtomicBoolean droppedFlag;
    private final AtomicBoolean closedFlag;
    private final Set<VersionUsage> versionUsages;

    NitriteMVMap(final MVMap<Key, Value> mvMap, final NitriteStore<?> nitriteStore) {
        this.mvMap = mvMap;
        this.nitriteStore = nitriteStore;
        this.mvStore = mvMap.getStore();
        this.closedFlag = new AtomicBoolean(false);
        this.droppedFlag = new AtomicBoolean(false);
        this.versionUsages = ConcurrentHashMap.newKeySet();
    }

    @Override
    public boolean containsKey(final Key key) {
        return mvMap.containsKey(key);
    }

    @Override
    public Value get(final Key key) {
        return mvMap.get(key);
    }

    @Override
    public NitriteStore<?> getStore() {
        return nitriteStore;
    }

    @Override
    public void clear() {
        final MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
        try {
            mvMap.clear();
            updateLastModifiedTime();
        } finally {
            mvStore.deregisterVersionUsage(txCounter);
        }
    }

    @Override
    public String getName() {
        return mvMap.getName();
    }

    @Override
    public RecordStream<Value> values() {
        return () -> versionedIterator(() -> mvMap.values().iterator());
    }

    @Override
    public Value remove(final Key key) {
        final MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
        try {
            final Value value = mvMap.remove(key);
            updateLastModifiedTime();
            return value;
        } finally {
            mvStore.deregisterVersionUsage(txCounter);
        }
    }

    @Override
    public RecordStream<Key> keys() {
        return () -> versionedIterator(() -> mvMap.keySet().iterator());
    }

    @Override
    public void put(final Key key, final Value value) {
        notNull(value, "value cannot be null");
        final MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
        try {
            mvMap.put(key, value);
            updateLastModifiedTime();
        } finally {
            mvStore.deregisterVersionUsage(txCounter);
        }
    }

    @Override
    public long size() {
        return mvMap.sizeAsLong();
    }

    @Override
    public Value putIfAbsent(final Key key, final Value value) {
        notNull(value, "value cannot be null");
        final MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
        try {
            final Value v = mvMap.putIfAbsent(key, value);
            updateLastModifiedTime();
            return v;
        } finally {
            mvStore.deregisterVersionUsage(txCounter);
        }
    }

    @Override
    public RecordStream<Pair<Key, Value>> entries() {
        return () -> versionedIterator(() -> new Iterator<>() {
            private final Iterator<Map.Entry<Key, Value>> entryIterator = mvMap.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public Pair<Key, Value> next() {
                final Map.Entry<Key, Value> entry = entryIterator.next();
                return new Pair<>(entry.getKey(), entry.getValue());
            }
        });
    }

    @Override
    public RecordStream<Pair<Key, Value>> reversedEntries() {
        return () -> versionedIterator(() -> new ReverseIterator<>(mvMap));
    }

    private <Element> Iterator<Element> versionedIterator(final Supplier<Iterator<Element>> iteratorSupplier) {
        return new VersionedIterator<>(mvStore, iteratorSupplier, versionUsages);
    }

    @Override
    public Key firstKey() {
        return mvMap.firstKey();
    }

    @Override
    public Key lastKey() {
        return mvMap.lastKey();
    }

    @Override
    public Key higherKey(final Key key) {
        return mvMap.higherKey(key);
    }

    @Override
    public Key ceilingKey(final Key key) {
        return mvMap.ceilingKey(key);
    }

    @Override
    public Key lowerKey(final Key key) {
        return mvMap.lowerKey(key);
    }

    @Override
    public Key floorKey(final Key key) {
        return mvMap.floorKey(key);
    }

    @Override
    public boolean isEmpty() {
        return mvMap.isEmpty();
    }

    @Override
    public void drop() {
        if (!droppedFlag.get()) {
            droppedFlag.compareAndSet(false, true);
            closedFlag.compareAndSet(false, true);
            releaseVersionUsages();

            final MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
            try {
                nitriteStore.closeMap(mvMap.getName());
                nitriteStore.removeMap(mvMap.getName());
            } finally {
                mvStore.deregisterVersionUsage(txCounter);
            }
        }
    }

    @Override
    public boolean isDropped() {
        return droppedFlag.get();
    }

    @Override
    public void close() {
        if (!closedFlag.get() && !droppedFlag.get()) {
            closedFlag.compareAndSet(false, true);
            releaseVersionUsages();
            nitriteStore.closeMap(mvMap.getName());
        }
    }

    @Override
    public boolean isClosed() {
        return closedFlag.get();
    }

    private void releaseVersionUsages() {
        for (final VersionUsage versionUsage : versionUsages) {
            versionUsage.release();
        }
    }

    private static class VersionedIterator<Element> implements Iterator<Element> {

        private final Iterator<Element> iterator;
        private final Cleaner.Cleanable cleanable;
        private final VersionUsage versionUsage;
        private boolean exhausted;

        private VersionedIterator(final MVStore mvStore,
                                  final Supplier<Iterator<Element>> iteratorSupplier,
                                  final Set<VersionUsage> versionUsages) {

            versionUsage = new VersionUsage(mvStore, mvStore.registerVersionUsage(), versionUsages);
            versionUsages.add(versionUsage);

            try {
                this.iterator = iteratorSupplier.get();
                this.cleanable = CLEANER.register(this, versionUsage::release);
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
                final boolean hasNext = iterator.hasNext();
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
        public Element next() {
            if (exhausted) {
                throw new NoSuchElementException();
            }
            ensureOpen();
            try {
                return iterator.next();
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
