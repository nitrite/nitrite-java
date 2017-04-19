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
class EqualsObjectFilter extends BaseObjectFilter {
    private String field;
    private Object value;

    EqualsObjectFilter(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        validateSearchTerm(nitriteMapper, field, value);

        Filter eqFilter;
        if (nitriteMapper.isValueType(value)) {
            eqFilter = Filters.eq(field, nitriteMapper.asValue(value));
        } else {
            eqFilter = Filters.eq(field, value);
        }

        eqFilter.setNitriteService(nitriteService);
        return eqFilter.apply(documentMap);
    }
}
