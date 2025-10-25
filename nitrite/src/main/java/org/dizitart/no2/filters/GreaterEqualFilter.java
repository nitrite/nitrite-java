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

package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.Comparables;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.IndexMap;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

import static org.dizitart.no2.common.util.Numbers.compare;

/**
 * @author Anindya Chatterjee
 */
class GreaterEqualFilter extends SortingAwareFilter {
    GreaterEqualFilter(String field, Comparable<?> value) {
        super(field, value);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean apply(Pair<NitriteId, Document> element) {
        Comparable comparable = getComparable();
        Document document = element.getSecond();
        Object fieldValue = document.get(getField());
        if (fieldValue != null) {
            if (fieldValue instanceof Number && comparable instanceof Number) {
                return compare((Number) fieldValue, (Number) comparable) >= 0;
            } else if (fieldValue instanceof Comparable) {
                Comparable arg = (Comparable) fieldValue;
                return arg.compareTo(comparable) >= 0;
            } else {
                throw new FilterException(fieldValue + " is not comparable");
            }
        }

        return false;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<?> applyOnIndex(IndexMap indexMap) {
        Comparable comparable = getComparable();
        List<NavigableMap<Comparable<?>, Object>> subMaps = new ArrayList<>();

        // maintain the find sorting order
        List<NitriteId> nitriteIds = new ArrayList<>();

        // Check if this is a compound index by looking at the first value
        Comparable firstKey = indexMap.firstKey();
        boolean isCompoundIndex = firstKey != null && indexMap.get(firstKey) instanceof NavigableMap;
        
        // For compound indexes or non-numeric comparisons, use the efficient range approach
        // For single-field numeric indexes, scan all entries to handle cross-type comparisons
        boolean useFullScan = !isCompoundIndex && comparable instanceof Number && firstKey instanceof Number;

        if (isReverseScan()) {
            // if reverse scan is required, then start from the last key
            Comparable lastKey = indexMap.lastKey();
            if (useFullScan) {
                // Full scan with numeric comparison for single-field numeric indexes
                while(lastKey != null) {
                    if (compare((Number) lastKey, (Number) comparable) >= 0) {
                        Object value = indexMap.get(lastKey);
                        processIndexValue(value, subMaps, nitriteIds);
                    }
                    lastKey = indexMap.lowerKey(lastKey);
                }
            } else {
                // Efficient range scan for compound indexes or non-numeric comparisons
                while(lastKey != null && Comparables.compare(lastKey, comparable) >= 0) {
                    Object value = indexMap.get(lastKey);
                    processIndexValue(value, subMaps, nitriteIds);
                    lastKey = indexMap.lowerKey(lastKey);
                }
            }
        } else {
            if (useFullScan) {
                // Full scan with numeric comparison for single-field numeric indexes
                Comparable key = indexMap.firstKey();
                while (key != null) {
                    if (compare((Number) key, (Number) comparable) >= 0) {
                        Object value = indexMap.get(key);
                        processIndexValue(value, subMaps, nitriteIds);
                    }
                    key = indexMap.higherKey(key);
                }
            } else {
                // Efficient range scan for compound indexes or non-numeric comparisons
                Comparable ceilingKey = indexMap.ceilingKey(comparable);
                while (ceilingKey != null) {
                    Object value = indexMap.get(ceilingKey);
                    processIndexValue(value, subMaps, nitriteIds);
                    ceilingKey = indexMap.higherKey(ceilingKey);
                }
            }
        }

        if (!subMaps.isEmpty()) {
            // if sub-map is populated then filtering on compound index, return sub-map
            return subMaps;
        } else {
            // else it is filtering on either single field index,
            // or it is a terminal filter on compound index, return only nitrite-ids
            return nitriteIds;
        }
    }

    @Override
    public String toString() {
        return "(" + getField() + " >= " + getValue() + ")";
    }
}
