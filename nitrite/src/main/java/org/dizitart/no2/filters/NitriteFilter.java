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
import lombok.Setter;
import org.dizitart.no2.NitriteConfig;

import java.util.Objects;

/**
 * An abstract class representing a filter for Nitrite database.
 *
 * @author Anindya Chatterjee.
 * @since 4.0
 */
@Getter
@Setter
public abstract class NitriteFilter implements Filter {
    private NitriteConfig nitriteConfig;
    private String collectionName;
    private Boolean objectFilter = false;

    /**
     * Creates an and filter which performs a logical AND operation on two filters and selects
     * the documents that satisfy both filters.
     * <p>
     *
     * @param filter other filter
     * @return the and filter
     */
    public Filter and(Filter filter) {
        return new AndFilter(this, filter);
    }

    /**
     * Creates an or filter which performs a logical OR operation on two filters and selects
     * the documents that satisfy at least one of the filter.
     * <p>
     *
     * @param filter other filter
     * @return the or filter
     */
    public Filter or(Filter filter) {
        return new OrFilter(this, filter);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NitriteFilter) {
            return Objects.equals(this.toString(), String.valueOf(o));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
