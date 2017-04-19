package org.dizitart.no2.filters;

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@ToString
class NotFilter extends BaseFilter {
    private Filter filter;

    NotFilter(Filter filter) {
        this.filter = filter;
    }

    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> resultSet = new LinkedHashSet<>(documentMap.keySet());
        resultSet.removeAll(filter.apply(documentMap));
        return resultSet;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        if (filter != null && filter instanceof BaseFilter) {
            filter.setNitriteService(nitriteService);
        }

        return matchedSet(documentMap);
    }
}
