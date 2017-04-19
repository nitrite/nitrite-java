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
class AndObjectFilter extends BaseObjectFilter {
    private ObjectFilter[] filters;

    AndObjectFilter(final ObjectFilter... filters) {
        this.filters = filters;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        for (ObjectFilter filter : filters) {
            filter.setNitriteService(nitriteService);
            filter.setNitriteMapper(nitriteMapper);
        }
        Filter and = Filters.and(filters);
        and.setNitriteService(nitriteService);
        return and.apply(documentMap);
    }
}
