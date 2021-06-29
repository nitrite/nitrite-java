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

package org.dizitart.no2.filters;

import org.dizitart.no2.common.mapper.NitriteMapper;

import static org.dizitart.no2.common.util.ValidationUtils.*;

/**
 * @author Anindya Chatterjee
 */
abstract class ComparableArrayFilter extends ComparableFilter {
    /**
     * Instantiates a new Comparable filter.
     *
     * @param field the field
     * @param value the value
     */
    public ComparableArrayFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    protected void validateSearchTerm(NitriteMapper nitriteMapper, String field, Object value) {
        notNull(field, "field cannot be null");
        notEmpty(field, "field cannot be empty");

        if (value != null) {
            if (value.getClass().isArray()) {
                validateFilterArrayField(value, field);
            } else if (value instanceof Iterable) {
                validateFilterIterableField((Iterable<?>) value, field);
            }
        }
    }
}
