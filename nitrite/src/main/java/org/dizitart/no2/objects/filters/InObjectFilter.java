package org.dizitart.no2.objects.filters;

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.store.NitriteMap;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.FE_IN_SEARCH_TERM_NOT_COMPARABLE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.ValidationUtils.validateInFilterValue;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@ToString
class InObjectFilter extends BaseObjectFilter {
    private String field;
    private Object[] values;
    private List<Object> objectList;

    InObjectFilter(String field, Object... values) {
        this.field = field;
        this.values = values;
        this.objectList = Arrays.asList(values);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        validateInFilterValue(field, values);

        Object[] valueArray = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            if (!nitriteMapper.isValueType(values[i]) || !(values[i] instanceof Comparable)) {
                throw new FilterException(errorMessage("search term " + values[i] + " is not a comparable",
                        FE_IN_SEARCH_TERM_NOT_COMPARABLE));
            }
            if (nitriteMapper.isValueType(values[i])) {
                valueArray[i] = nitriteMapper.asValue(values[i]);
            } else {
                valueArray[i] = values[i];
            }
        }

        Filter in = Filters.in(field, valueArray);
        in.setNitriteService(nitriteService);
        return in.apply(documentMap);
    }
}
