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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.RecordIterable;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.store.NitriteMap;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import static org.dizitart.no2.exceptions.ErrorMessage.REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED;

/**
 * @author Anindya Chatterjee.
 */
class ProjectedDocumentIterable implements RecordIterable<Document> {
    private final Collection<NitriteId> resultSet;
    private final NitriteMap<NitriteId, Document> underlyingMap;
    private Document projection;
    private boolean hasMore;
    private int totalCount;

    ProjectedDocumentIterable(Document projection, FindResult findResult) {
        this.projection = projection;
        if (findResult.getIdSet() != null) {
            resultSet = findResult.getIdSet();
        } else {
            resultSet = new TreeSet<>();
        }
        this.underlyingMap = findResult.getUnderlyingMap();
        this.hasMore = findResult.isHasMore();
        this.totalCount = findResult.getTotalCount();
    }

    @NotNull
    @Override
    public Iterator<Document> iterator() {
        return new ProjectedDocumentIterator();
    }

    @Override
    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public int size() {
        return resultSet.size();
    }

    @Override
    public int totalCount() {
        return totalCount;
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class ProjectedDocumentIterator implements Iterator<Document> {
        private Iterator<NitriteId> iterator;
        private Document nextElement = null;

        ProjectedDocumentIterator() {
            iterator = resultSet.iterator();
            nextMatch();
        }

        @Override
        public boolean hasNext() {
            return nextElement != null;
        }

        @Override
        public Document next() {
            Document returnValue = nextElement;
            nextMatch();
            return returnValue;
        }

        private void nextMatch() {
            while (iterator.hasNext()) {
                NitriteId next = iterator.next();
                Document document = underlyingMap.get(next);
                Document projected = project(document);
                if (projected != null) {
                    nextElement = projected;
                    return;
                }
            }

            nextElement = null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException(REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED);
        }

        private Document project(Document original) {
            if (projection == null) return original;
            Document result = new Document(original);

            for (KeyValuePair keyValuePair : original) {
                if (!projection.containsKey(keyValuePair.getKey())) {
                    result.remove(keyValuePair.getKey());
                }
            }
            return result;
        }
    }
}
