/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.filters.Filter;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Anindya Chatterjee.
 */
class FilteredRecordStream implements RecordStream<KeyValuePair<NitriteId, Document>> {
    private final RecordStream<KeyValuePair<NitriteId, Document>> recordStream;
    private final Filter filter;

    FilteredRecordStream(RecordStream<KeyValuePair<NitriteId, Document>> recordStream, Filter filter) {
        this.recordStream = recordStream;
        this.filter = filter;
    }

    @Override
    public Iterator<KeyValuePair<NitriteId, Document>> iterator() {
        Iterator<KeyValuePair<NitriteId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new FilteredIterator(iterator, filter);
    }

    static class FilteredIterator implements Iterator<KeyValuePair<NitriteId, Document>> {
        private final Iterator<KeyValuePair<NitriteId, Document>> iterator;
        private final Filter filter;
        private KeyValuePair<NitriteId, Document> nextPair;
        private boolean nextPairSet = false;

        public FilteredIterator(Iterator<KeyValuePair<NitriteId, Document>> iterator, Filter filter) {
            this.iterator = iterator;
            this.filter = filter;
        }

        @Override
        public boolean hasNext() {
            return nextPairSet || setNextId();
        }

        @Override
        public KeyValuePair<NitriteId, Document> next() {
            if (!nextPairSet && !setNextId()) {
                throw new NoSuchElementException();
            }
            nextPairSet = false;
            return nextPair;
        }

        @Override
        public void remove() {
            if (nextPairSet) {
                throw new InvalidOperationException("remove operation cannot be called here");
            }
            iterator.remove();
        }

        private boolean setNextId() {
            while (iterator.hasNext()) {
                final KeyValuePair<NitriteId, Document> keyValuePair = iterator.next();
                if (filter.apply(keyValuePair)) {
                    nextPair = keyValuePair;
                    nextPairSet = true;
                    return true;
                }
            }
            return false;
        }
    }

}
