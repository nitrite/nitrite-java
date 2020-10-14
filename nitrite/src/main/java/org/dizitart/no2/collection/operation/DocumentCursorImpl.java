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
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;

import java.text.Collator;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Anindya Chatterjee.
 */
class DocumentCursorImpl implements DocumentCursor {
    private final RecordStream<Pair<NitriteId, Document>> recordStream;
    private FindOptions findOptions;

    DocumentCursorImpl(RecordStream<Pair<NitriteId, Document>> recordStream) {
        this.recordStream = recordStream;
    }

    @Override
    public DocumentCursor sort(Fields fields, Collator collator, NullOrder nullOrder) {
        findOptions = new FindOptions();
        findOptions.collator(collator);
        findOptions.nullOrder(nullOrder);
        findOptions.sortBy(fields);

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
        Iterator<Pair<NitriteId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new DocumentCursorIterator(iterator);
    }

    private void validateProjection(Document projection) {
        for (Pair<String, Object> kvp : projection) {
            validateKeyValuePair(kvp);
        }
    }

    private void validateKeyValuePair(Pair<String, Object> kvp) {
        if (kvp.getSecond() != null) {
            if (!(kvp.getSecond() instanceof Document)) {
                throw new ValidationException("projection contains non-null values");
            } else {
                validateProjection((Document) kvp.getSecond());
            }
        }
    }

    private static class DocumentCursorIterator implements Iterator<Document> {
        private final Iterator<Pair<NitriteId, Document>> iterator;

        DocumentCursorIterator(Iterator<Pair<NitriteId, Document>> iterator) {
            this.iterator = iterator;
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
