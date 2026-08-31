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
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.h2.mvstore.MVStore;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class VersionUsage {

    /**
     * Shared by every iterator and cursor in the adapter - one daemon thread is enough to release
     * the versions of iterators that were abandoned rather than drained.
     */
    static final Cleaner CLEANER = Cleaner.create();

    private final AtomicBoolean released = new AtomicBoolean(false);

    private final MVStore mvStore;
    private final MVStore.TxCounter txCounter;
    private final Set<VersionUsage> versionUsages;

    boolean isReleased() {
        return released.get();
    }

    void release() {
        if (released.compareAndSet(false, true)) {
            versionUsages.remove(this);
            mvStore.deregisterVersionUsage(txCounter);
        }
    }
}
