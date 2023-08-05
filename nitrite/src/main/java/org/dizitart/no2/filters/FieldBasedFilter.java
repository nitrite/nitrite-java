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

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.mapper.NitriteMapper;

import java.util.List;
import java.util.NavigableMap;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * Represents a filter based on value of a nitrite document field.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class FieldBasedFilter extends NitriteFilter {
    private String field;

    @Getter(AccessLevel.NONE)
    private Object value;

    @Getter(AccessLevel.NONE)
    private boolean processed = false;

    /**
     * Instantiates a new Field based filter.
     *
     * @param field the field
     * @param value the value
     */
    protected FieldBasedFilter(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    /**
     * Gets the value of the filter.
     *
     * @return the value
     */
    public Object getValue() {
        if (this.processed) return value;

        if (value == null) return null;

        if (getObjectFilter()) {
            NitriteMapper nitriteMapper = getNitriteConfig().nitriteMapper();
            validateSearchTerm(nitriteMapper, field, value);
            if (value instanceof Comparable) {
                value = nitriteMapper.tryConvert(value, Comparable.class);
            }
        }

        this.processed = true;
        return value;
    }

    protected void validateSearchTerm(NitriteMapper nitriteMapper, String field, Object value) {
        notNull(field, "field cannot be null");
        notEmpty(field, "field cannot be empty");
    }

    /**
     * Process values after index scanning.
     *
     * @param value      the value
     * @param subMap     the sub map
     * @param nitriteIds the nitrite ids
     */
    @SuppressWarnings("unchecked")
    protected void processIndexValue(Object value,
                                     List<NavigableMap<Comparable<?>, Object>> subMap,
                                     List<NitriteId> nitriteIds) {
        if (value instanceof List) {
            // if it is list then add it directly to nitrite ids
            List<NitriteId> result = (List<NitriteId>) value;
            nitriteIds.addAll(result);
        }

        if (value instanceof NavigableMap) {
            subMap.add((NavigableMap<Comparable<?>, Object>) value);
        }
    }
}
