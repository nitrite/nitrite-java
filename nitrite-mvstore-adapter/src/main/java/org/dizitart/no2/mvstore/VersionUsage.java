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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.h2.mvstore.MVStore;

class VersionUsage {

    /**
     * Shared by every iterator and cursor in the adapter - one daemon thread is enough to release
     * the versions of iterators that were abandoned rather than drained.
     */
    static final Cleaner CLEANER = Cleaner.create();

    /**
     * Every unreleased usage of each store. A map's close() and drop() release the usages of that
     * map, but an iterator opened through a wrapper already closed or dropped is no longer reachable
     * from any map the store knows, and would hold its version until it is collected. The store
     * releases what is left here before it closes, so H2 never closes under a held version.
     */
    private static final Map<MVStore, Set<VersionUsage>> OPEN_USAGES =
        new ConcurrentHashMap<>();

    private final AtomicBoolean released = new AtomicBoolean(false);

    private final MVStore mvStore;
    private final MVStore.TxCounter txCounter;
    private final Set<VersionUsage> versionUsages;
    private final Set<VersionUsage> storeUsages;

    VersionUsage(final MVStore mvStore, final MVStore.TxCounter txCounter, final Set<VersionUsage> versionUsages) {
        this.mvStore = mvStore;
        this.txCounter = txCounter;
        this.versionUsages = versionUsages;
        this.storeUsages = OPEN_USAGES.computeIfAbsent(mvStore, store -> ConcurrentHashMap.newKeySet());
        storeUsages.add(this);
    }

    static void releaseAll(final MVStore mvStore) {
        final Set<VersionUsage> usages = OPEN_USAGES.remove(mvStore);
        if (usages != null) {
            for (final VersionUsage usage : usages) {
                usage.release();
            }
        }
    }

    boolean isReleased() {
        return released.get();
    }

    void release() {
        if (released.compareAndSet(false, true)) {
            versionUsages.remove(this);
            storeUsages.remove(this);
            mvStore.deregisterVersionUsage(txCounter);
        }
    }
}
