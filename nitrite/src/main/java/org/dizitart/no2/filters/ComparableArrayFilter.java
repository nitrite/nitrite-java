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

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.mapper.NitriteMapper;

import java.util.HashSet;
import java.util.Set;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ValidationUtils.*;

/**
 * An abstract class representing a filter that operates on a field with a comparable array value.
 * 
 * @author Anindya Chatterjee
 * @since 4.0
 */
public abstract class ComparableArrayFilter extends ComparableFilter {
    
    public ComparableArrayFilter(String field, Object value) {
        super(field, value);
    }

    /**
     * When filtering on the _id field, resolves the search terms to {@link NitriteId}s
     * so matching happens by id like eq/getById do. Databases written before 4.4 store
     * the _id field as a String, so comparing raw field values would silently miss
     * those legacy ids. Returns {@code null} for any other field.
     */
    static Set<NitriteId> toNitriteIdSet(String field, Comparable<?>[] values) {
        if (!DOC_ID.equals(field)) {
            return null;
        }
        Set<NitriteId> idSet = new HashSet<>();
        for (Comparable<?> value : values) {
            NitriteId nitriteId = toNitriteId(field, value);
            if (nitriteId != null) {
                idSet.add(nitriteId);
            }
        }
        return idSet;
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
