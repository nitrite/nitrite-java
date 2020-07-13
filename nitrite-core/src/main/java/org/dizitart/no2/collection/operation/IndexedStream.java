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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.filters.IndexAwareFilter;
import org.dizitart.no2.store.NitriteMap;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
class IndexedStream implements RecordStream<KeyValuePair<NitriteId, Document>> {
    private final Set<NitriteId> nitriteIds;
    private final NitriteMap<NitriteId, Document> nitriteMap;

    IndexedStream(IndexAwareFilter indexAwareFilter,
                  NitriteMap<NitriteId, Document> nitriteMap) {
        this.nitriteMap = nitriteMap;

        nitriteIds = indexAwareFilter.getOnIdField()
            ? indexAwareFilter.cachedIds(nitriteMap)
            : indexAwareFilter.cachedIndexedIds();
    }

    @Override
    public Iterator<KeyValuePair<NitriteId, Document>> iterator() {
        return new IndexedStreamIterator(nitriteIds.iterator(), nitriteMap);
    }

    static class IndexedStreamIterator implements Iterator<KeyValuePair<NitriteId, Document>> {
        private final Iterator<NitriteId> iterator;
        private final NitriteMap<NitriteId, Document> nitriteMap;

        IndexedStreamIterator(Iterator<NitriteId> iterator,
                              NitriteMap<NitriteId, Document> nitriteMap) {
            this.iterator = iterator;
            this.nitriteMap = nitriteMap;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public KeyValuePair<NitriteId, Document> next() {
            NitriteId id = iterator.next();
            Document document = nitriteMap.get(id);
            return new KeyValuePair<>(id, document);
        }
    }
}
