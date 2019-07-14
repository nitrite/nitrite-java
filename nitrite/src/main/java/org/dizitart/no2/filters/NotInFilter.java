package org.dizitart.no2.filters;

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;

import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.dizitart.no2.util.ValidationUtils.validateInFilterValue;

/**
 * @author Anindya Chatterjee
 */
@Getter
@ToString
class NotInFilter extends BaseFilter {
    private String field;
    private Object[] values;
    private Set<Object> objectList;

    NotInFilter(String field, Object... values) {
        this.field = field;
        this.values = values;
        this.objectList = new HashSet<>();
        Collections.addAll(this.objectList, values);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        validateInFilterValue(field, values);

        if (nitriteService.hasIndex(field)
                && !nitriteService.isIndexing(field) && objectList != null) {
            return nitriteService.findNotInWithIndex(field, objectList);
        } else {
            return matchedSet(documentMap);
        }
    }

    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = getFieldValue(document, field);
            if (!objectList.contains(fieldValue)) {
                nitriteIdSet.add(entry.getKey());
            }
        }
        return nitriteIdSet;
    }


}
