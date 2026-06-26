package org.dizitart.no2.filters;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SortingAwareFilter extends ComparableFilter {

    /**
     * The comparison operator (&gt;, &gt;=, &lt;, &lt;=) a {@link SortingAwareFilter}
     * represents. Used by the index scanner to combine a lower and an upper bound into a
     * single bounded range scan.
     */
    public enum ComparisonMode {
        Greater,
        GreaterEqual,
        Lesser,
        LesserEqual
    }

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

    /**
     * The comparison operator this filter represents.
     *
     * @return the comparison mode
     */
    public abstract ComparisonMode getComparisonMode();
}
