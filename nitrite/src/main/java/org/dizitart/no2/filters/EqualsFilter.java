package org.dizitart.no2.filters;

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.Constants.DOC_ID;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.dizitart.no2.util.EqualsUtils.deepEquals;

@Getter
@ToString
class EqualsFilter extends BaseFilter {
    private String field;
    private Object value;

    EqualsFilter(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        if (field.equals(DOC_ID)) {
            Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
            NitriteId nitriteId = null;
            if (value instanceof Long) {
                nitriteId = NitriteId.createId((Long) value);
            }

            if (nitriteId != null) {
                if (documentMap.containsKey(nitriteId)) {
                    nitriteIdSet.add(nitriteId);
                }
            }
            return nitriteIdSet;
        } else if (nitriteService.hasIndex(field)
                && !nitriteService.isIndexing(field)
                && value != null) {
            return nitriteService.findEqualWithIndex(field, value);
        } else {
            return matchedSet(documentMap);
        }
    }

    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = getFieldValue(document, field);
            if (deepEquals(fieldValue, value)) {
                nitriteIdSet.add(entry.getKey());
            }
        }
        return nitriteIdSet;
    }
}
