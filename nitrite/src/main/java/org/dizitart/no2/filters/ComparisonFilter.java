package org.dizitart.no2.filters;

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.internals.NitriteService;

import static org.dizitart.no2.exceptions.ErrorMessage.VALUE_IS_NOT_COMPARABLE;

@Getter
@ToString
abstract class ComparisonFilter extends BaseFilter {
    protected String field;
    protected Comparable comparable;

    ComparisonFilter(String field, Object value) {
        if (value instanceof Comparable) {
            this.comparable = (Comparable) value;
        } else {
            throw new FilterException(VALUE_IS_NOT_COMPARABLE);
        }
        this.field = field;
    }

    @Override
    public void setNitriteService(NitriteService nitriteService) {
        this.nitriteService = nitriteService;
    }
}
