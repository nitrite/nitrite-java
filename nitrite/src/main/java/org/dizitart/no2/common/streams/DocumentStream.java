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

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.common.processors.ProcessorChain;

import java.util.Collections;
import java.util.Iterator;

/**
 * @since 4.0
 * @author Anindya Chatterjee.
 */
public class DocumentStream implements DocumentCursor {
    private final RecordStream<Pair<NitriteId, Document>> recordStream;
    private final ProcessorChain processorChain;

    @Getter @Setter
    private FindPlan findPlan;

    public DocumentStream(RecordStream<Pair<NitriteId, Document>> recordStream,
                          ProcessorChain processorChain) {
        this.recordStream = recordStream;
        this.processorChain = processorChain;
    }

    @Override
    public RecordStream<Document> project(Document projection) {
        validateProjection(projection);
        return new ProjectedDocumentStream(recordStream, projection, processorChain);
    }

    @Override
    public RecordStream<Document> join(DocumentCursor foreignCursor, Lookup lookup) {
        return new JoinedDocumentStream(recordStream, foreignCursor, lookup, processorChain);
    }

    @Override
    public Iterator<Document> iterator() {
        Iterator<Pair<NitriteId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new DocumentCursorIterator(iterator, processorChain);
    }

    private void validateProjection(Document projection) {
        for (Pair<String, Object> kvp : projection) {
            validateKeyValuePair(kvp);
        }
    }

    private void validateKeyValuePair(Pair<String, Object> kvp) {
        if (kvp.getSecond() != null) {
            if (!(kvp.getSecond() instanceof Document)) {
                throw new ValidationException("Projection contains non-null values");
            } else {
                validateProjection((Document) kvp.getSecond());
            }
        }
    }

    private static class DocumentCursorIterator implements Iterator<Document> {
        private final Iterator<Pair<NitriteId, Document>> iterator;
        private final ProcessorChain processorChain;

        DocumentCursorIterator(Iterator<Pair<NitriteId, Document>> iterator,
                               ProcessorChain processorChain) {
            this.iterator = iterator;
            this.processorChain = processorChain;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Document next() {
            Pair<NitriteId, Document> next = iterator.next();
            Document document = next.getSecond();
            if (document != null) {
                Document copy = document.clone();
                copy = processorChain.processAfterRead(copy);
                return copy;
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("Remove on cursor is not supported");
        }
    }
}
