package org.dizitart.no2.objects.filters;

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.store.NitriteMap;

import java.util.Set;

import static org.dizitart.no2.util.ValidationUtils.validateSearchTerm;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@ToString
class GreaterObjectFilter extends BaseObjectFilter {
    private String field;
    private Object value;

    GreaterObjectFilter(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        validateSearchTerm(nitriteMapper, field, value);
        Comparable comparable;
        if (nitriteMapper.isValueType(value)) {
            comparable = (Comparable) nitriteMapper.asValue(value);
        } else  {
            comparable = (Comparable) value;
        }

        Filter gt = Filters.gt(field, comparable);
        gt.setNitriteService(nitriteService);
        return gt.apply(documentMap);
    }
}
