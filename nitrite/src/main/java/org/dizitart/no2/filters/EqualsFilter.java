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
import org.dizitart.no2.index.IndexMap;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;

/**
 * @author Anindya Chatterjee.
 */
class EqualsFilter extends ComparableFilter {
    EqualsFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    public boolean apply(Pair<NitriteId, Document> element) {
        Document document = element.getSecond();
        Object fieldValue = document.get(getField());
        return deepEquals(fieldValue, getValue());
    }

    @Override
    public List<?> applyOnIndex(IndexMap indexMap) {
        Object value = indexMap.get((Comparable<?>) getValue());
        if (value instanceof List) {
            return ((List<?>) value);
        }

        List<Object> result = new ArrayList<>();
        result.add(value);
        return result;
    }

    @Override
    public String toString() {
        return "(" + getField() + " == " + getValue() + ")";
    }
}
