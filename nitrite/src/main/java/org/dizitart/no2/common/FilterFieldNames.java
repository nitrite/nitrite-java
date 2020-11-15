package org.dizitart.no2.common;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.filters.Filter;

/**
 * @author Anindya Chatterjee
 */
@Getter @Setter
public class FilterFieldNames extends FieldNames {
    private Filter filter;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
