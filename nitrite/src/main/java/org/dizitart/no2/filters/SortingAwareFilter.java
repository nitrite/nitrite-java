package org.dizitart.no2.filters;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SortingAwareFilter extends ComparableFilter {

    /**
     * Indicates if the filter should scan the index in reverse order.
     */
    private boolean reverseScan;

    /**
     * Instantiates a new SortingAwareFilter.
     *
     * @param field the field
     * @param value the value
     */
    public SortingAwareFilter(String field, Object value) {
        super(field, value);
    }
}
