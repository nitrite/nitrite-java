/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.common.streams;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.tuples.Pair;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts documents based on the sort order provided.
 *
 * <p>
 * By default null is considered the lowest value,
 * unless ordering explicitly specified by {@link NullOrder}.
 * </p>
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class DocumentSorter implements Comparator<Pair<NitriteId, Document>> {
    private final Collator collator;
    private final NullOrder nullOrder;
    private final List<Pair<String, SortOrder>> sortOrder;

    /**
     * Instantiates a new Document sorter.
     *
     * @param collator  the collator
     * @param nullOrder the null order
     * @param sortOrder the sort order
     */
    public DocumentSorter(Collator collator, NullOrder nullOrder,
                          List<Pair<String, SortOrder>> sortOrder) {
        this.collator = collator;
        this.nullOrder = nullOrder;
        this.sortOrder = sortOrder;
    }

    @Override
    @SuppressWarnings( { "rawtypes", "unchecked" })
    public int compare(Pair<NitriteId, Document> pair1, Pair<NitriteId, Document> pair2) {
        if (sortOrder != null && !sortOrder.isEmpty()) {
            for (Pair<String, SortOrder> pair : sortOrder) {
                Document doc1 = pair1.getSecond();
                Document doc2 = pair2.getSecond();

                Comparable c1 = doc1.get(pair.getFirst(), Comparable.class);
                Comparable c2 = doc2.get(pair.getFirst(), Comparable.class);

                boolean nullPresent = false;
                int result;

                if (c1 == null && c2 != null) {
                    nullPresent = true;
                    if (nullOrder == NullOrder.First || nullOrder == NullOrder.Default) {
                        result = -1;
                    } else {
                        result = 1;
                    }
                } else if (c1 != null && c2 == null) {
                    nullPresent = true;
                    if (nullOrder == NullOrder.First || nullOrder == NullOrder.Default) {
                        result = 1;
                    } else {
                        result = -1;
                    }
                } else if (c2 == null) {
                    nullPresent = true;
                    result = 0;
                } else if (c1 instanceof String && c2 instanceof String && collator != null) {
                    result = collator.compare(c1, c2);
                } else {
                    result = c1.compareTo(c2);
                }

                if (!nullPresent && pair.getSecond() == SortOrder.Descending) {
                    result *= -1;
                }

                if (result != 0) {
                    return result;
                }
            }
        }
        return 0;
    }
}
