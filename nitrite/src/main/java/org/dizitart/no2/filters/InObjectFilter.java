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

package org.dizitart.no2.filters;

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Filter;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.store.NitriteMap;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.FE_IN_SEARCH_TERM_NOT_COMPARABLE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.ValidationUtils.validateInFilterValue;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@ToString
class InObjectFilter extends BaseObjectFilter {
    private String field;
    private Object[] values;
    private List<Object> objectList;

    InObjectFilter(String field, Object... values) {
        this.field = field;
        this.values = values;
        this.objectList = Arrays.asList(values);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        validateInFilterValue(field, values);

        Object[] valueArray = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            if (!nitriteMapper.isValueType(values[i]) || !(values[i] instanceof Comparable)) {
                throw new FilterException(errorMessage("search term " + values[i] + " is not a comparable",
                        FE_IN_SEARCH_TERM_NOT_COMPARABLE));
            }
            if (nitriteMapper.isValueType(values[i])) {
                valueArray[i] = nitriteMapper.asValue(values[i]);
            } else {
                valueArray[i] = values[i];
            }
        }

        Filter in = Filters.in(field, valueArray);
        in.setIndexedQueryTemplate(indexedQueryTemplate);
        return in.apply(documentMap);
    }
}
