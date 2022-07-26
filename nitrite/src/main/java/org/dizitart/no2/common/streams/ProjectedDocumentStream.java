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
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.common.processors.ProcessorChain;

import java.util.Collections;
import java.util.Iterator;

/**
 * Represents a projected nitrite document stream.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public class ProjectedDocumentStream implements RecordStream<Document> {
    private final RecordStream<Pair<NitriteId, Document>> recordStream;
    private final Document projection;
    private final ProcessorChain processorChain;

    /**
     * Instantiates a new Projected document stream.
     *
     * @param recordStream   the record stream
     * @param projection     the projection
     * @param processorChain the processor chain
     */
    public ProjectedDocumentStream(RecordStream<Pair<NitriteId, Document>> recordStream,
                                   Document projection, ProcessorChain processorChain) {
        this.recordStream = recordStream;
        this.projection = projection;
        this.processorChain = processorChain;
    }

    @Override
    public Iterator<Document> iterator() {
        Iterator<Pair<NitriteId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new ProjectedDocumentIterator(iterator, processorChain, projection);
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private static class ProjectedDocumentIterator implements Iterator<Document> {
        private final Iterator<Pair<NitriteId, Document>> iterator;
        private final ProcessorChain processorChain;
        private Document nextElement = null;
        private final Document projection;

        /**
         * Instantiates a new Projected document iterator.
         *
         * @param iterator       the iterator
         * @param processorChain the processor chain
         */
        ProjectedDocumentIterator(Iterator<Pair<NitriteId, Document>> iterator,
                                  ProcessorChain processorChain,
                                  Document projection) {
            this.iterator = iterator;
            this.processorChain = processorChain;
            this.projection = projection;
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
            throw new InvalidOperationException("Remove on a cursor is not supported");
        }

        private Document project(Document original) {
            if (projection == null) return original;
            Document newDoc = Document.createDocument();

            for (String field : projection.getFields()) {
                if (original.containsField(field)) {
                    Object value = original.get(field);
                    newDoc.put(field, value);
                }
            }

            // process the result
            newDoc = processorChain.processAfterRead(newDoc);
            return newDoc;
        }
    }
}

