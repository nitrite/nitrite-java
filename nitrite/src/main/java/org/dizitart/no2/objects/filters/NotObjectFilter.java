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
class NotObjectFilter extends BaseObjectFilter {
    private ObjectFilter filter;

    NotObjectFilter(ObjectFilter filter) {
        this.filter = filter;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        filter.setNitriteService(nitriteService);
        filter.setNitriteMapper(nitriteMapper);

        Filter not = Filters.not(filter);
        not.setNitriteService(nitriteService);
        return not.apply(documentMap);
    }
}
