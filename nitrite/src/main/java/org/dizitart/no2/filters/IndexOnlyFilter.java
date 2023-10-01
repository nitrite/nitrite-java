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

/**
 * An abstract class representing a filter that can be applied to an index.
 * <p>
 * NOTE: This filter does not support collection scan.
 *
 * @since 4.0
 * @author Anindya Chatterjee
 */
public abstract class IndexOnlyFilter extends ComparableFilter {
    /**
     * Instantiates a new {@link IndexOnlyFilter}.
     *
     * @param field the field
     * @param value the value
     */
    public IndexOnlyFilter(String field, Object value) {
        super(field, value);
    }

    /**
     * Gets the supported index type for this filter.
     *
     * @return the supported index type
     */
    public abstract String supportedIndexType();

    /**
     * Checks if <code>other</code> filter can be grouped together with this filter.
     *
     * @param other the comparable filter
     * @return the boolean
     */
    public abstract boolean canBeGrouped(IndexOnlyFilter other);
}
