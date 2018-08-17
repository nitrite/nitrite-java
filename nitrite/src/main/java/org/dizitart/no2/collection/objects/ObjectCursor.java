/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.objects;

import org.dizitart.no2.Document;
import org.dizitart.no2.collection.Lookup;
import org.dizitart.no2.collection.RecordIterable;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.mapper.NitriteMapper;

import javax.validation.constraints.NotNull;
import java.util.Iterator;

import static org.dizitart.no2.exceptions.ErrorCodes.VE_PROJECT_NULL_PROJECTION;
import static org.dizitart.no2.exceptions.ErrorMessage.OBJ_REMOVE_ON_OBJECT_ITERATOR_NOT_SUPPORTED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.DocumentUtils.emptyDocument;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 * */
class ObjectCursor<T> implements Cursor<T> {
    private org.dizitart.no2.collection.Cursor cursor;
    private NitriteMapper nitriteMapper;
    private Class<T> type;

    ObjectCursor(NitriteMapper nitriteMapper, org.dizitart.no2.collection.Cursor cursor, Class<T> type) {
        this.nitriteMapper = nitriteMapper;
        this.cursor = cursor;
        this.type = type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P> RecordIterable<P> project(Class<P> projectionType) {
        notNull(projectionType, errorMessage("projection can not be null", VE_PROJECT_NULL_PROJECTION));
        Document dummyDoc = emptyDocument(nitriteMapper, projectionType);
        return new ProjectedObjectIterable<>(nitriteMapper, cursor.project(dummyDoc), projectionType);
    }

    @Override
    public <Foreign, Joined> RecordIterable<Joined> join(Cursor<Foreign> foreignCursor,
                                                         Lookup lookup, Class<Joined> type) {
        ObjectCursor<Foreign> foreignObjectCursor = (ObjectCursor<Foreign>) foreignCursor;
        return new JoinedObjectIterable<>(nitriteMapper, cursor.join(foreignObjectCursor.cursor, lookup), type);
    }

    @Override
    public boolean hasMore() {
        return cursor.hasMore();
    }

    @Override
    public int size() {
        return cursor.size();
    }

    @Override
    public int totalCount() {
        return cursor.totalCount();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new ObjectCursorIterator(cursor.iterator());
    }

    private class ObjectCursorIterator implements Iterator<T> {
        private Iterator<Document> documentIterator;

        ObjectCursorIterator(Iterator<Document> documentIterator) {
            this.documentIterator = documentIterator;
        }

        @Override
        public boolean hasNext() {
            return documentIterator.hasNext();
        }

        @Override
        public T next() {
            Document document = documentIterator.next();
            if (document != null) {
                return nitriteMapper.asObject(document, type);
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException(OBJ_REMOVE_ON_OBJECT_ITERATOR_NOT_SUPPORTED);
        }
    }
}
