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

import java.util.*;

/**
 * Represents an union of multiple distinct nitrite document stream.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class UnionStream implements RecordStream<Pair<NitriteId, Document>> {
    private final Collection<RecordStream<Pair<NitriteId, Document>>> streams;

    /**
     * Instantiates a new Union stream.
     *
     * @param streams the streams
     */
    public UnionStream(Collection<RecordStream<Pair<NitriteId, Document>>> streams) {
        this.streams = streams;
    }

    @Override
    public Iterator<Pair<NitriteId, Document>> iterator() {
        Queue<Iterator<Pair<NitriteId, Document>>> iteratorQueue = new LinkedList<>();
        for (RecordStream<Pair<NitriteId, Document>> stream : streams) {
            iteratorQueue.add(stream.iterator());
        }
        return new UnionStreamIterator(iteratorQueue);
    }

    /**
     * The type Union stream iterator.
     */
    private static class UnionStreamIterator implements Iterator<Pair<NitriteId, Document>> {
        private final Queue<Iterator<Pair<NitriteId, Document>>> iteratorQueue;
        private Iterator<Pair<NitriteId, Document>> currentIterator;

        /**
         * Instantiates a new Union stream iterator.
         *
         * @param iteratorQueue the iterator queue
         */
        public UnionStreamIterator(Queue<Iterator<Pair<NitriteId, Document>>> iteratorQueue) {
            this.iteratorQueue = iteratorQueue;
        }

        @Override
        public boolean hasNext() {
            updateCurrentIterator();
            return currentIterator.hasNext();
        }

        @Override
        public Pair<NitriteId, Document> next() {
            updateCurrentIterator();
            return currentIterator.next();
        }


        private void updateCurrentIterator() {
            if (currentIterator == null) {
                if (iteratorQueue.isEmpty()) {
                    currentIterator = Collections.emptyIterator();
                } else {
                    currentIterator = iteratorQueue.remove();
                }
            }

            while (!currentIterator.hasNext() && !iteratorQueue.isEmpty()) {
                currentIterator = iteratorQueue.remove();
            }
        }
    }
}
