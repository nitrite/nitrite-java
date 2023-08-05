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

package org.dizitart.no2.repository;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.streams.MutatedObjectStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;

import java.lang.reflect.Modifier;
import java.util.Iterator;

import static org.dizitart.no2.common.util.DocumentUtils.skeletonDocument;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.common.util.ValidationUtils.validateProjectionType;

/**
 * @author Anindya Chatterjee.
 */
class ObjectCursor<T> implements Cursor<T> {
    private final DocumentCursor cursor;
    private final NitriteMapper nitriteMapper;
    private final Class<T> type;

    public ObjectCursor(NitriteMapper nitriteMapper, DocumentCursor cursor, Class<T> type) {
        this.nitriteMapper = nitriteMapper;
        this.cursor = cursor;
        this.type = type;
    }

    @Override
    public long size() {
        return cursor.size();
    }

    @Override
    public FindPlan getFindPlan() {
        return cursor.getFindPlan();
    }

    @Override
    public <P> RecordStream<P> project(Class<P> projectionType) {
        notNull(projectionType, "projection cannot be null");
        Document dummyDoc = emptyDocument(nitriteMapper, projectionType);
        return new MutatedObjectStream<>(nitriteMapper, cursor.project(dummyDoc), projectionType);
    }

    @Override
    public <Foreign, Joined> RecordStream<Joined> join(Cursor<Foreign> foreignCursor, Lookup lookup, Class<Joined> type) {
        ObjectCursor<Foreign> foreignObjectCursor = (ObjectCursor<Foreign>) foreignCursor;
        return new MutatedObjectStream<>(nitriteMapper, cursor.join(foreignObjectCursor.cursor, lookup), type);
    }

    @Override
    public Iterator<T> iterator() {
        return new ObjectCursorIterator(cursor.iterator());
    }

    private <D> Document emptyDocument(NitriteMapper nitriteMapper, Class<D> type) {
        if (type.isPrimitive()) {
            throw new ValidationException("Cannot project to primitive type");
        } else if (type.isInterface()) {
            throw new ValidationException("Cannot project to interface");
        } else if (type.isArray()) {
            throw new ValidationException("Cannot project to array");
        } else if (Modifier.isAbstract(type.getModifiers())) {
            throw new ValidationException("Cannot project to abstract type");
        }

        validateProjectionType(type, nitriteMapper);

        Document dummyDoc = skeletonDocument(nitriteMapper, type);
        if (dummyDoc == null || dummyDoc.size() == 0) {
            throw new ValidationException("Cannot project to empty type");
        } else {
            return dummyDoc;
        }
    }

    private class ObjectCursorIterator implements Iterator<T> {
        private final Iterator<Document> documentIterator;

        ObjectCursorIterator(Iterator<Document> documentIterator) {
            this.documentIterator = documentIterator;
        }

        @Override
        public boolean hasNext() {
            return documentIterator.hasNext();
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            Document document = documentIterator.next();
            return (T) nitriteMapper.tryConvert(document, type);
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("Remove on a cursor is not supported");
        }
    }
}
