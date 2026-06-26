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

import lombok.Getter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Anindya Chatterjee
 */
@Getter
class InFilter extends ComparableArrayFilter {
    private final Set<Comparable<?>> comparableSet;

    InFilter(String field, Comparable<?>... values) {
        super(field, values);
        this.comparableSet = new HashSet<>();
        Collections.addAll(this.comparableSet, values);
    }

    @Override
    public boolean apply(Pair<NitriteId, Document> element) {
        Document document = element.getSecond();
        Object fieldValue = document.get(getField());

        if (fieldValue instanceof Comparable) {
            Comparable<?> comparable = (Comparable<?>) fieldValue;
            return comparableSet.contains(comparable);
        }
        return false;
    }

    public List<?> applyOnIndex(IndexMap indexMap) {
        // convert comparable set to DBValue set
        Set<DBValue> dbValueSet = comparableSet.stream().map(value -> value == null ? DBNull.getInstance()
            : new DBValue(value)).collect(Collectors.toSet());

        List<NavigableMap<DBValue, Object>> subMap = new ArrayList<>();
        List<NitriteId> nitriteIds = new ArrayList<>();

        for (Pair<DBValue, ?> entry : indexMap.entries()) {
            if (dbValueSet.contains(entry.getFirst())) {
                processIndexValue(entry.getSecond(), subMap, nitriteIds);
            }
        }

        if (!subMap.isEmpty()) {
            // if sub-map is populated then filtering on compound index, return sub-map
            return subMap;
        } else {
            // else it is filtering on either single field index,
            // or it is a terminal filter on compound index, return only nitrite-ids
            return nitriteIds;
        }
    }

    @Override
    public String toString() {
        return "(" + getField() + " in " + Arrays.toString((Comparable<?>[]) getValue()) + ")";
    }
}
