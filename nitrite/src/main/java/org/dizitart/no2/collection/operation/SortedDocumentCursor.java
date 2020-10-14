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
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.ValidationException;

import java.text.Collator;
import java.util.*;

/**
 * @author Anindya Chatterjee.
 */
class SortedDocumentCursor implements RecordStream<Pair<NitriteId, Document>> {
    private final String field;
    private final SortOrder sortOrder;
    private final Collator collator;
    private final NullOrder nullOrder;
    private final RecordStream<Pair<NitriteId, Document>> recordStream;

    public SortedDocumentCursor(String field,
                                SortOrder sortOrder,
                                Collator collator,
                                NullOrder nullOrder,
                                RecordStream<Pair<NitriteId, Document>> recordStream) {
        this.field = field;
        this.sortOrder = sortOrder;
        this.collator = collator;
        this.nullOrder = nullOrder;
        this.recordStream = recordStream;
    }

    @Override
    public Iterator<Pair<NitriteId, Document>> iterator() {
        Iterator<Pair<NitriteId, Document>> iterator = recordStream == null ? Collections.emptyIterator()
            : recordStream.iterator();
        return new SortedDocumentIterator(field, sortOrder, collator, nullOrder, iterator);
    }

    static class SortedDocumentIterator implements Iterator<Pair<NitriteId, Document>> {
        private final String field;
        private final SortOrder sortOrder;
        private final Collator collator;
        private final NullOrder nullOrder;
        private final Iterator<Pair<NitriteId, Document>> iterator;
        private Iterator<Pair<NitriteId, Document>> sortedIterator;

        public SortedDocumentIterator(String field,
                                      SortOrder sortOrder,
                                      Collator collator,
                                      NullOrder nullOrder,
                                      Iterator<Pair<NitriteId, Document>> iterator) {
            this.field = field;
            this.sortOrder = sortOrder;
            this.collator = collator;
            this.nullOrder = nullOrder;
            this.iterator = iterator;
            initialize();
        }

        @Override
        public boolean hasNext() {
            return sortedIterator.hasNext();
        }

        @Override
        public Pair<NitriteId, Document> next() {
            return sortedIterator.next();
        }

        private void initialize() {
            NavigableMap<Object, List<Pair<NitriteId, Document>>> sortedMap;
            if (collator != null) {
                sortedMap = new TreeMap<>(collator);
            } else {
                sortedMap = new TreeMap<>();
            }

            Set<Pair<NitriteId, Document>> nullValueEntries = new HashSet<>();
            while (iterator.hasNext()) {
                Pair<NitriteId, Document> next = iterator.next();
                Document document = next.getSecond();
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

                List<Pair<NitriteId, Document>> pairs;
                if (sortedMap.containsKey(value)) {
                    pairs = sortedMap.get(value);
                } else {
                    pairs = new ArrayList<>();
                }
                pairs.add(next);
                sortedMap.put(value, pairs);
            }

            List<Pair<NitriteId, Document>> sortedPairs;
            if (sortOrder == SortOrder.Ascending) {
                if (nullOrder == NullOrder.Default || nullOrder == NullOrder.First) {
                    sortedPairs = new ArrayList<>(nullValueEntries);
                    sortedPairs.addAll(flattenList(sortedMap.values()));
                } else {
                    sortedPairs = flattenList(sortedMap.values());
                    sortedPairs.addAll(nullValueEntries);
                }
            } else {
                if (nullOrder == NullOrder.Default || nullOrder == NullOrder.Last) {
                    sortedPairs = flattenList(sortedMap.descendingMap().values());
                    sortedPairs.addAll(nullValueEntries);
                } else {
                    sortedPairs = new ArrayList<>(nullValueEntries);
                    sortedPairs.addAll(flattenList(sortedMap.descendingMap().values()));
                }
            }

            this.sortedIterator = sortedPairs.iterator();
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
