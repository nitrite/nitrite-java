package org.dizitart.no2.filters;

import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.Constants.DOC_ID;
import static org.dizitart.no2.exceptions.ErrorCodes.FE_GTE_FIELD_NOT_COMPARABLE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.dizitart.no2.util.NumberUtils.compare;

@ToString
class GreaterEqualFilter extends ComparisonFilter {
    GreaterEqualFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        if (field.equals(DOC_ID)) {
            Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
            NitriteId nitriteId = null;
            if (comparable instanceof Long) {
                nitriteId = NitriteId.createId((Long) comparable);
            }

            if (nitriteId != null) {
                NitriteId ceilingKey = documentMap.ceilingKey(nitriteId);
                while (ceilingKey != null) {
                    nitriteIdSet.add(ceilingKey);
                    ceilingKey = documentMap.higherKey(ceilingKey);
                }
            }
            return nitriteIdSet;
        } else if (nitriteService.hasIndex(field)
                && !nitriteService.isIndexing(field)) {
            return nitriteService.findGreaterEqualWithIndex(field, comparable);
        } else {
            return matchedSet(documentMap);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = getFieldValue(document, field);
            if (fieldValue != null) {
                if (fieldValue instanceof Number && comparable instanceof Number) {
                    if (compare((Number) fieldValue, (Number) comparable) >= 0) {
                        nitriteIdSet.add(entry.getKey());
                    }
                } else if (fieldValue instanceof Comparable) {
                    Comparable arg = (Comparable) fieldValue;
                    if (arg.compareTo(comparable) >= 0) {
                        nitriteIdSet.add(entry.getKey());
                    }
                } else {
                    throw new FilterException(errorMessage(
                            fieldValue + " is not comparable",
                            FE_GTE_FIELD_NOT_COMPARABLE));
                }
            }
        }
        return nitriteIdSet;
    }
}
