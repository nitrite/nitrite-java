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

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Anindya Chatterjee.
 */
class ProjectedDocumentStream implements RecordStream<Document> {
    private final RecordStream<Pair<NitriteId, Document>> recordStream;
    private final Document projection;

    public ProjectedDocumentStream(RecordStream<Pair<NitriteId, Document>> recordStream,
                                   Document projection) {
        this.recordStream = recordStream;
        this.projection = projection;
    }

    @Override
    public Iterator<Document> iterator() {
        Iterator<Pair<NitriteId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new ProjectedDocumentIterator(iterator);
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class ProjectedDocumentIterator implements Iterator<Document> {
        private final Iterator<Pair<NitriteId, Document>> iterator;
        private Document nextElement = null;

        ProjectedDocumentIterator(Iterator<Pair<NitriteId, Document>> iterator) {
            this.iterator = iterator;
            nextMatch();
        }

        @Override
        public boolean hasNext() {
            return nextElement != null;
        }

        @Override
        public Document next() {
            Document returnValue = nextElement.clone();
            nextMatch();
            return returnValue;
        }

        private void nextMatch() {
            while (iterator.hasNext()) {
                Pair<NitriteId, Document> next = iterator.next();
                Document document = next.getSecond();
                if (document != null) {
                    Document projected = project(document.clone());
                    if (projected != null) {
                        nextElement = projected;
                        return;
                    }
                }
            }

            nextElement = null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on a cursor is not supported");
        }

        private Document project(Document original) {
            if (projection == null) return original;
            Document result = original.clone();

            for (Pair<String, Object> pair : original) {
                if (!projection.containsKey(pair.getFirst())) {
                    result.remove(pair.getFirst());
                }
            }
            return result;
        }
    }
}

