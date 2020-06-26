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
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Anindya Chatterjee.
 */
class ProjectedDocumentIterable implements ReadableStream<Document> {
    private final ReadableStream<NitriteId> readableStream;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final Document projection;

    public ProjectedDocumentIterable(ReadableStream<NitriteId> readableStream,
                                     NitriteMap<NitriteId, Document> nitriteMap,
                                     Document projection) {
        this.readableStream = readableStream;
        this.nitriteMap = nitriteMap;
        this.projection = projection;
    }

    @Override
    public Iterator<Document> iterator() {
        Iterator<NitriteId> iterator = readableStream == null ? Collections.emptyIterator()
            : readableStream.iterator();
        return new ProjectedDocumentIterator(iterator);
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class ProjectedDocumentIterator implements Iterator<Document> {
        private final Iterator<NitriteId> iterator;
        private Document nextElement = null;

        ProjectedDocumentIterator(Iterator<NitriteId> iterator) {
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
                NitriteId next = iterator.next();
                Document document = nitriteMap.get(next);
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

            for (KeyValuePair<String, Object> keyValuePair : original) {
                if (!projection.containsKey(keyValuePair.getKey())) {
                    result.remove(keyValuePair.getKey());
                }
            }
            return result;
        }
    }
}

