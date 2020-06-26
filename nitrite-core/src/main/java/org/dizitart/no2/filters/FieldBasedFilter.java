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
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.HashSet;
import java.util.Set;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class FieldBasedFilter extends NitriteFilter {
    private String field;

    @Getter(AccessLevel.NONE)
    private Object value;

    protected FieldBasedFilter(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    @SuppressWarnings("rawtypes")
    protected Set<Comparable> convertValues(Set<Comparable> values) {
        if (getObjectFilter()) {
            NitriteMapper nitriteMapper = getNitriteConfig().nitriteMapper();
            Set<Comparable> convertedValues = new HashSet<>();

            for (Comparable comparable : values) {
                if (comparable == null
                    || !nitriteMapper.isValue(comparable)) {
                    throw new FilterException("search term " + comparable
                        + " is not a comparable");
                }

                if (nitriteMapper.isValue(comparable)) {
                    Comparable convertValue = nitriteMapper.convert(comparable, Comparable.class);
                    convertedValues.add(convertValue);
                }
            }

            return convertedValues;
        }
        return values;
    }

    public Object getValue() {
        if (value == null) return null;

        if (getObjectFilter()) {
            NitriteMapper nitriteMapper = getNitriteConfig().nitriteMapper();
            validateSearchTerm(nitriteMapper, field, value);
            if (nitriteMapper.isValue(value)) {
                value = nitriteMapper.convert(value, Comparable.class);
            }
        }
        return value;
    }

    private void validateSearchTerm(NitriteMapper nitriteMapper, String field, Object value) {
        notNull(field, "field cannot be null");
        notEmpty(field, "field cannot be empty");

        if (value != null) {
            if (!nitriteMapper.isValue(value) && !(value instanceof Comparable)) {
                throw new ValidationException("search term is not comparable " + value);
            }
        }
    }
}
