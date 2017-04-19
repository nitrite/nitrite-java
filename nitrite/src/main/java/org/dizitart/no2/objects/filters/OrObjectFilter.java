package org.dizitart.no2.objects.filters;

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.store.NitriteMap;

import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@ToString
class OrObjectFilter extends BaseObjectFilter {
    private ObjectFilter[] filters;

    OrObjectFilter(ObjectFilter... filters) {
        this.filters = filters;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        for (ObjectFilter filter : filters) {
            filter.setNitriteService(nitriteService);
            filter.setNitriteMapper(nitriteMapper);
        }
        Filter or = Filters.or(filters);
        or.setNitriteService(nitriteService);
        return or.apply(documentMap);
    }
}
