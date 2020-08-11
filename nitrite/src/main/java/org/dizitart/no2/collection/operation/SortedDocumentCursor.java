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
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.ValidationException;

import java.text.Collator;
import java.util.*;

/**
 * @author Anindya Chatterjee.
 */
class SortedDocumentCursor implements RecordStream<KeyValuePair<NitriteId, Document>> {
    private final String field;
    private final SortOrder sortOrder;
    private final Collator collator;
    private final NullOrder nullOrder;
    private final RecordStream<KeyValuePair<NitriteId, Document>> recordStream;

    public SortedDocumentCursor(String field,
                                SortOrder sortOrder,
                                Collator collator,
                                NullOrder nullOrder,
                                RecordStream<KeyValuePair<NitriteId, Document>> recordStream) {
        this.field = field;
        this.sortOrder = sortOrder;
        this.collator = collator;
        this.nullOrder = nullOrder;
        this.recordStream = recordStream;
    }

    @Override
    public Iterator<KeyValuePair<NitriteId, Document>> iterator() {
        Iterator<KeyValuePair<NitriteId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new SortedDocumentIterator(field, sortOrder, collator, nullOrder, iterator);
    }

    static class SortedDocumentIterator implements Iterator<KeyValuePair<NitriteId, Document>> {
        private final String field;
        private final SortOrder sortOrder;
        private final Collator collator;
        private final NullOrder nullOrder;
        private final Iterator<KeyValuePair<NitriteId, Document>> iterator;
        private Iterator<KeyValuePair<NitriteId, Document>> sortedIterator;

        public SortedDocumentIterator(String field,
                                      SortOrder sortOrder,
                                      Collator collator,
                                      NullOrder nullOrder,
                                      Iterator<KeyValuePair<NitriteId, Document>> iterator) {
            this.field = field;
            this.sortOrder = sortOrder;
            this.collator = collator;
            this.nullOrder = nullOrder;
            this.iterator = iterator;
            init();
        }

        @Override
        public boolean hasNext() {
            return sortedIterator.hasNext();
        }

        @Override
        public KeyValuePair<NitriteId, Document> next() {
            return sortedIterator.next();
        }

        private void init() {
            NavigableMap<Object, List<KeyValuePair<NitriteId, Document>>> sortedMap;
            if (collator != null) {
                sortedMap = new TreeMap<>(collator);
            } else {
                sortedMap = new TreeMap<>();
            }

            Set<KeyValuePair<NitriteId, Document>> nullValueEntries = new HashSet<>();
            while (iterator.hasNext()) {
                KeyValuePair<NitriteId, Document> next = iterator.next();
                Document document = next.getValue();
                if (document == null) continue;

                Object value = document.get(field);
                if (value != null) {
                    if (value.getClass().isArray() || value instanceof Iterable) {
                        throw new ValidationException("cannot sort on an array or collection object");
                    }
                } else {
                    nullValueEntries.add(next);
                    continue;
                }

                List<KeyValuePair<NitriteId, Document>> keyValuePairs;
                if (sortedMap.containsKey(value)) {
                    keyValuePairs = sortedMap.get(value);
                } else {
                    keyValuePairs = new ArrayList<>();
                }
                keyValuePairs.add(next);
                sortedMap.put(value, keyValuePairs);
            }

            List<KeyValuePair<NitriteId, Document>> sortedKeyValuePairs;
            if (sortOrder == SortOrder.Ascending) {
                if (nullOrder == NullOrder.Default || nullOrder == NullOrder.First) {
                    sortedKeyValuePairs = new ArrayList<>(nullValueEntries);
                    sortedKeyValuePairs.addAll(flattenList(sortedMap.values()));
                } else {
                    sortedKeyValuePairs = flattenList(sortedMap.values());
                    sortedKeyValuePairs.addAll(nullValueEntries);
                }
            } else {
                if (nullOrder == NullOrder.Default || nullOrder == NullOrder.Last) {
                    sortedKeyValuePairs = flattenList(sortedMap.descendingMap().values());
                    sortedKeyValuePairs.addAll(nullValueEntries);
                } else {
                    sortedKeyValuePairs = new ArrayList<>(nullValueEntries);
                    sortedKeyValuePairs.addAll(flattenList(sortedMap.descendingMap().values()));
                }
            }

            this.sortedIterator = sortedKeyValuePairs.iterator();
        }

        private <E> List<E> flattenList(Collection<List<E>> collection) {
            List<E> finalList = new ArrayList<>();
            for (List<E> list : collection) {
                finalList.addAll(list);
            }
            return finalList;
        }
    }
}
