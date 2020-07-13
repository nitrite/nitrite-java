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
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.*;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;

import java.text.Collator;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Anindya Chatterjee.
 */
class DocumentCursorImpl implements DocumentCursor {
    private final RecordStream<KeyValuePair<NitriteId, Document>> recordStream;

    DocumentCursorImpl(RecordStream<KeyValuePair<NitriteId, Document>> recordStream) {
        this.recordStream = recordStream;
    }

    @Override
    public DocumentCursor sort(String field, SortOrder sortOrder, Collator collator, NullOrder nullOrder) {
        return new DocumentCursorImpl(new SortedDocumentCursor(field, sortOrder, collator,
            nullOrder, recordStream));
    }

    @Override
    public DocumentCursor skipLimit(long skip, long limit) {
        return new DocumentCursorImpl(new BoundedDocumentStream(recordStream, skip, limit));
    }

    @Override
    public RecordStream<Document> project(Document projection) {
        validateProjection(projection);
        return new ProjectedDocumentStream(recordStream, projection);
    }

    @Override
    public RecordStream<Document> join(DocumentCursor foreignCursor, Lookup lookup) {
        return new JoinedDocumentStream(recordStream, foreignCursor, lookup);
    }

    @Override
    public Iterator<Document> iterator() {
        Iterator<KeyValuePair<NitriteId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new DocumentCursorIterator(iterator);
    }

    private void validateProjection(Document projection) {
        for (KeyValuePair<String, Object> kvp : projection) {
            validateKeyValuePair(kvp);
        }
    }

    private void validateKeyValuePair(KeyValuePair<String, Object> kvp) {
        if (kvp.getValue() != null) {
            if (!(kvp.getValue() instanceof Document)) {
                throw new ValidationException("projection contains non-null values");
            } else {
                validateProjection((Document) kvp.getValue());
            }
        }
    }

    private static class DocumentCursorIterator implements Iterator<Document> {
        private final Iterator<KeyValuePair<NitriteId, Document>> iterator;

        DocumentCursorIterator(Iterator<KeyValuePair<NitriteId, Document>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Document next() {
            KeyValuePair<NitriteId, Document> next = iterator.next();
            Document document = next.getValue();
            if (document != null) {
                return document.clone();
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on cursor is not supported");
        }
    }
}
