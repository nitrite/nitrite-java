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
     * Filter by id.
     *
     * @param nitriteId the nitrite id
     * @return the filter
     */
    static Filter byId(NitriteId nitriteId) {
        return new EqualsFilter(DOC_ID, nitriteId.getIdValue());
    }

    /**
     * And filter.
     *
     * @param filters the filters
     * @return the filter
     */
    static Filter and(Filter... filters) {
        notEmpty(filters, "At least two filters must be specified");
        if (filters.length < 2) {
            throw new FilterException("at least two filters must be specified");
        }

        return new AndFilter(filters);
    }

    /**
     * Or filter.
     *
     * @param filters the filters
     * @return the filter
     */
    static Filter or(Filter... filters) {
        notEmpty(filters, "at least two filters must be specified");
        if (filters.length < 2) {
            throw new FilterException("at least two filters must be specified");
        }

        return new OrFilter(filters);
    }

    /**
     * Filters a document map and returns <code>true</code> if the criteria matches.
     *
     * @param element the entry to check.
     * @return boolean value to indicate if the filtering criteria matches the document.
     */
    boolean apply(Pair<NitriteId, Document> element);

    /**
     * Creates a not filter which performs a logical NOT operation on a filter and selects
     * the documents that *_do not_* satisfy the criteria. This also includes documents
     * that do not contain the value.
     * <p>
     *
     * @return the not filter
     */
    default Filter not() {
        return new NotFilter(this);
    }
}
