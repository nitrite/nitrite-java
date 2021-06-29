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
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.common.mapper.NitriteMapper;

import java.util.Iterator;

import static org.dizitart.no2.common.Constants.DOC_ID;

/**
 * @author Anindya Chatterjee.
 */
class MutatedObjectStream<T> implements RecordStream<T> {
    private final RecordStream<Document> recordIterable;
    private final Class<T> mutationType;
    private final NitriteMapper nitriteMapper;

    MutatedObjectStream(NitriteMapper nitriteMapper,
                        RecordStream<Document> recordIterable,
                        Class<T> mutationType) {
        this.recordIterable = recordIterable;
        this.mutationType = mutationType;
        this.nitriteMapper = nitriteMapper;
    }

    @Override
    public Iterator<T> iterator() {
        return new MutatedObjectIterator(nitriteMapper);
    }

    private class MutatedObjectIterator implements Iterator<T> {
        private final NitriteMapper nitriteMapper;
        private final Iterator<Document> documentIterator;

        MutatedObjectIterator(NitriteMapper nitriteMapper) {
            this.nitriteMapper = nitriteMapper;
            this.documentIterator = recordIterable.iterator();
        }

        @Override
        public boolean hasNext() {
            return documentIterator.hasNext();
        }

        @Override
        public T next() {
            Document item = documentIterator.next();
            if (item != null) {
                Document record = item.clone();
                record.remove(DOC_ID);
                return nitriteMapper.convert(record, mutationType);
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on a cursor is not supported");
        }
    }
}
