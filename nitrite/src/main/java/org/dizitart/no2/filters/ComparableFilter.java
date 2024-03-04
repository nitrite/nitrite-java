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

import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.IndexMap;

import java.util.List;

/**
 * An abstract class representing a filter that compares fields.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public abstract class ComparableFilter extends FieldBasedFilter {
    /**
     * Instantiates a new Comparable filter.
     *
     * @param field the field
     * @param value the value
     */
    public ComparableFilter(String field, Object value) {
        super(field, value);
    }

    /**
     * Gets the {@link Comparable} value to filter.
     *
     * @return the comparable
     */
    @SuppressWarnings("rawtypes")
    public Comparable getComparable() {
        if (getValue() == null) {
            throw new FilterException("value parameter must not be null");
        }
        return (Comparable) getValue();
    }

    /**
     * Apply this filter on a nitrite index.
     *
     * @param indexMap the index scanner
     * @return the object
     */
    public abstract List<?> applyOnIndex(IndexMap indexMap);
}
