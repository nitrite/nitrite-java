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
import org.dizitart.no2.collection.RecordIterable;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.mapper.NitriteMapper;

import javax.validation.constraints.NotNull;
import java.util.Iterator;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.exceptions.ErrorMessage.OBJ_REMOVE_ON_PROJECTED_OBJECT_ITERATOR_NOT_SUPPORTED;

/**
 * @author Anindya Chatterjee.
 */
class ProjectedObjectIterable<T> implements RecordIterable<T> {
    private RecordIterable<Document> recordIterable;
    private Class<T> projectionType;
    private NitriteMapper nitriteMapper;

    ProjectedObjectIterable(NitriteMapper nitriteMapper,
                            RecordIterable<Document> recordIterable,
                            Class<T> projectionType) {
        this.recordIterable = recordIterable;
        this.projectionType = projectionType;
        this.nitriteMapper = nitriteMapper;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new ProjectedObjectIterator(nitriteMapper);
    }

    @Override
    public boolean hasMore() {
        return recordIterable.hasMore();
    }

    @Override
    public int size() {
        return recordIterable.size();
    }

    @Override
    public int totalCount() {
        return recordIterable.totalCount();
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class ProjectedObjectIterator implements Iterator<T> {
        private NitriteMapper objectMapper;
        private Iterator<Document> documentIterator;

        ProjectedObjectIterator(NitriteMapper nitriteMapper) {
            this.objectMapper = nitriteMapper;
            this.documentIterator = recordIterable.iterator();
        }

        @Override
        public boolean hasNext() {
            return documentIterator.hasNext();
        }

        @Override
        public T next() {
            Document record = new Document(documentIterator.next());
            record.remove(DOC_ID);
            return objectMapper.asObject(record, projectionType);
        }

        @Override
        public void remove() {
            throw new InvalidOperationException(OBJ_REMOVE_ON_PROJECTED_OBJECT_ITERATOR_NOT_SUPPORTED);
        }
    }
}
