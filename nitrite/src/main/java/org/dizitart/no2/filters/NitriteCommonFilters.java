package org.dizitart.no2.filters;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.exceptions.FilterException;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;

public interface NitriteCommonFilters extends Filter {
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



}
