/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
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

package org.dizitart.no2.common.streams;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.store.NitriteMap;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class IndexedStream implements RecordStream<Pair<NitriteId, Document>> {
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final Iterable<NitriteId> nitriteIds;

    public IndexedStream(Iterable<NitriteId> nitriteIds,
                  NitriteMap<NitriteId, Document> nitriteMap) {
        this.nitriteIds = nitriteIds;
        this.nitriteMap = nitriteMap;
    }

    @Override
    public Iterator<Pair<NitriteId, Document>> iterator() {
        return new IndexedStreamIterator(nitriteIds.iterator(), nitriteMap);
    }

    /**
     * Counts the ids the index supplied, walking the id source only, without fetching a
     * single document.
     *
     * @return the number of ids
     */
    public long countIds() {
        long count = 0;
        for (NitriteId ignored : nitriteIds) {
            count++;
        }
        return count;
    }

    private static class IndexedStreamIterator implements Iterator<Pair<NitriteId, Document>>,
            SkippableIterator {
        private final Iterator<NitriteId> iterator;
        private final NitriteMap<NitriteId, Document> nitriteMap;
        private Pair<NitriteId, Document> next;

        IndexedStreamIterator(Iterator<NitriteId> iterator,
                              NitriteMap<NitriteId, Document> nitriteMap) {
            this.iterator = iterator;
            this.nitriteMap = nitriteMap;
        }

        @Override
        public boolean hasNext() {
            return next != null || advance();
        }

        /**
         * The index already gave the matching ids, so a skipped row costs one pointer here rather
         * than the map lookup and the decode that {@link #next()} would have paid for it.
         */
        @Override
        public long skip(long count) {
            long skipped = 0;
            if (next != null && count > 0) {
                next = null;
                skipped++;
            }
            while (skipped < count && iterator.hasNext()) {
                iterator.next();
                skipped++;
            }
            return skipped;
        }

        @Override
        public Pair<NitriteId, Document> next() {
            if (next == null && !advance()) {
                throw new NoSuchElementException();
            }
            Pair<NitriteId, Document> current = next;
            next = null;
            return current;
        }

        /**
         * A document removed between the index lookup and the fetch is no longer a row: it is
         * skipped rather than handed downstream as a null for a residual filter to dereference.
         */
        private boolean advance() {
            while (iterator.hasNext()) {
                NitriteId id = iterator.next();
                Document document = nitriteMap.get(id);
                if (document != null) {
                    next = new Pair<>(id, document);
                    return true;
                }
            }
            return false;
        }
    }
}
