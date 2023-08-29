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
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.FilterException;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;

/**
 * An interface to specify filtering criteria during find operation. When
 * a filter is applied to a collection, based on the criteria it returns
 * a set of matching records.
 * <p>
 * Each filtering criteria is based on a value of a document. If the value
 * is indexed, the find operation takes the advantage of it and only scans
 * the index map for that value. But if the value is not indexed, it scans
 * the whole collection.
 * </p>
 *
 * @author Anindya Chatterjee
 * @see NitriteCollection#find(Filter) NitriteCollection#find(Filter)
 * @see NitriteCollection#find(Filter, org.dizitart.no2.collection.FindOptions) NitriteCollection#find(Filter, org.dizitart.no2.collection.FindOptions)
 * @since 1.0
 */
public interface Filter {
    /**
     * A filter to select all elements.
     */
    Filter ALL = element -> true;

    /**
     * Returns a filter that matches documents with the specified NitriteId.
     * <p>
     * The returned filter matches documents where the value of the <b>_id</b> field 
     * is equal to the specified NitriteId's idValue.
     * 
     * 
     * @param nitriteId the nitrite id
     * @return the filter
     */
    static Filter byId(NitriteId nitriteId) {
        return new EqualsFilter(DOC_ID, nitriteId.getIdValue());
    }

    /**
     * Creates a filter that performs a logical AND operation on two or more filters.
     * The returned filter accepts a document if all filters in the list accept the document.
     * 
     * @param filters the filters to AND together
     * @return the new filter
     * @throws FilterException if less than two filters are specified
     */
    static Filter and(Filter... filters) {
        notEmpty(filters, "At least two filters must be specified");
        if (filters.length < 2) {
            throw new FilterException("At least two filters must be specified");
        }

        return new AndFilter(filters);
    }

    /**
     * Creates a filter that performs a logical OR operation on two or more filters.
     * The returned filter selects all documents that satisfy at least one of the filters in the list.
     * 
     * @param filters the filters to be combined using the OR operation
     * @return the filter that performs the OR operation on the specified filters
     * @throws FilterException if less than two filters are specified
     */
    static Filter or(Filter... filters) {
        notEmpty(filters, "At least two filters must be specified");
        if (filters.length < 2) {
            throw new FilterException("At least two filters must be specified");
        }

        return new OrFilter(filters);
    }

    /**
     * Applies the filter to the given element.
     *
     * @param element the element to apply the filter to.
     * @return {@code true} if the element matches the filter, {@code false} otherwise.
     */
    boolean apply(Pair<NitriteId, Document> element);

    /**
     * Creates a not filter which performs a logical NOT operation on a filter and selects
     * the documents that <strong>do not</strong> satisfy the criteria. 
     * <p>
     * NOTE: This also includes documents that do not contain the value.
     *
     * @return the not filter
     */
    default Filter not() {
        return new NotFilter(this);
    }
}
