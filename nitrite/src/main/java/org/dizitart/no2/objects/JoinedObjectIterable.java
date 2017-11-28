/*
 *
 * Copyright 2017 Nitrite author or authors.
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

package org.dizitart.no2.objects;

import org.dizitart.no2.Document;
import org.dizitart.no2.RecordIterable;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.util.Iterables;

import java.util.Iterator;
import java.util.List;

import static org.dizitart.no2.Constants.DOC_ID;
import static org.dizitart.no2.exceptions.ErrorMessage.OBJ_REMOVE_ON_JOINED_OBJECT_ITERATOR_NOT_SUPPORTED;

/**
 * @author Anindya Chatterjee
 */
class JoinedObjectIterable<T> implements RecordIterable<T> {
    private RecordIterable<Document> recordIterable;
    private Class<T> joinType;
    private JoinedObjectIterator iterator;
    private NitriteMapper nitriteMapper;

    JoinedObjectIterable(NitriteMapper nitriteMapper,
                         RecordIterable<Document> recordIterable,
                         Class<T> joinType) {
        this.recordIterable = recordIterable;
        this.joinType = joinType;
        this.nitriteMapper = nitriteMapper;
        this.iterator = new JoinedObjectIterator(nitriteMapper);
    }

    @Override
    public void reset() {
        this.recordIterable.reset();
        this.iterator = new JoinedObjectIterator(nitriteMapper);
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
    public T firstOrDefault() {
        T item = Iterables.firstOrDefault(this);
        reset();
        return item;
    }

    @Override
    public List<T> toList() {
        List<T> list = Iterables.toList(this);
        reset();
        return list;
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class JoinedObjectIterator implements Iterator<T> {
        private NitriteMapper objectMapper;

        JoinedObjectIterator(NitriteMapper nitriteMapper) {
            this.objectMapper = nitriteMapper;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = true;
            try {
                hasNext = recordIterable.iterator().hasNext();
                return hasNext;
            } finally {
                if (!hasNext) reset();
            }
        }

        @Override
        public T next() {
            Document record = new Document(recordIterable.iterator().next());
            record.remove(DOC_ID);
            return objectMapper.asObject(record, joinType);
        }

        @Override
        public void remove() {
            throw new InvalidOperationException(OBJ_REMOVE_ON_JOINED_OBJECT_ITERATOR_NOT_SUPPORTED);
        }
    }
}
