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

/**
 * An abstract class representing a filter for string values.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public abstract class StringFilter extends ComparableFilter {
    private final StringFilterHelper helper = new StringFilterHelper();

    /**
     * Instantiates a new String filter.
     *
     * @param field the field
     * @param value the value
     */
    protected StringFilter(String field, String value) {
        super(field, value);
    }

    /**
     * Gets string value.
     *
     * @return the string value
     */
    public String getStringValue() {
        return (String) getValue();
    }

    /**
     * Converts an object to a string safely.
     * @param obj the object to convert
     * @return the string representation, or empty if null
     */
    protected String toStringValue(Object obj) {
        return helper.toStringValue(obj);
    }

    public abstract boolean applyOnString(String value);
}
