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
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.DBValue;
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
class GreaterThanFilter extends SortingAwareFilter {
    protected GreaterThanFilter(String field, Comparable<?> value) {
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
                return compare((Number) fieldValue, (Number) comparable) > 0;
            } else if (fieldValue instanceof Comparable) {
                Comparable arg = (Comparable) fieldValue;
                return arg.compareTo(comparable) > 0;
            } else {
                throw new FilterException(fieldValue + " is not comparable");
            }
        }

        return false;
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public List<?> applyOnIndex(IndexMap indexMap) {
        Comparable comparable = getComparable();
        DBValue dbValue = comparable == null ? DBNull.getInstance() : new DBValue(comparable);
        List<NavigableMap<DBValue, Object>> subMaps = new ArrayList<>();
        List<NitriteId> nitriteIds = new ArrayList<>();

        if (isReverseScan()) {
            DBValue lastKey = indexMap.lastKey();
            while (lastKey != DBNull.getInstance() && Comparables.compare(lastKey, dbValue) > 0) {
                // get the starting value, it can be a navigable-map (compound index)
                // or list (single field index)
                Object value = indexMap.get(lastKey);
                processIndexValue(value, subMaps, nitriteIds);
                lastKey = indexMap.lowerKey(lastKey);
            }
        } else {
            DBValue higherKey = indexMap.higherKey(dbValue);
            while (higherKey != DBNull.getInstance()) {
                // get the starting value, it can be a navigable-map (compound index)
                // or list (single field index)
                Object value = indexMap.get(higherKey);
                processIndexValue(value, subMaps, nitriteIds);
                higherKey = indexMap.higherKey(higherKey);
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
        return "(" + getField() + " > " + getValue() + ")";
    }
}
