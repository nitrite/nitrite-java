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
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.filters.Filter;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Anindya Chatterjee.
 */
public class FilteredRecordStream implements RecordStream<Pair<NitriteId, Document>> {
    private final RecordStream<Pair<NitriteId, Document>> recordStream;
    private final Filter filter;

    FilteredRecordStream(RecordStream<Pair<NitriteId, Document>> recordStream, Filter filter) {
        this.recordStream = recordStream;
        this.filter = filter;
    }

    @Override
    public Iterator<Pair<NitriteId, Document>> iterator() {
        Iterator<Pair<NitriteId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new FilteredIterator(iterator, filter);
    }

    static class FilteredIterator implements Iterator<Pair<NitriteId, Document>> {
        private final Iterator<Pair<NitriteId, Document>> iterator;
        private final Filter filter;
        private Pair<NitriteId, Document> nextPair;
        private boolean nextPairSet = false;

        public FilteredIterator(Iterator<Pair<NitriteId, Document>> iterator, Filter filter) {
            this.iterator = iterator;
            this.filter = filter;
        }

        @Override
        public boolean hasNext() {
            return nextPairSet || setNextId();
        }

        @Override
        public Pair<NitriteId, Document> next() {
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
                final Pair<NitriteId, Document> pair = iterator.next();
                if (filter.apply(pair)) {
                    nextPair = pair;
                    nextPairSet = true;
                    return true;
                }
            }
            return false;
        }
    }

}
