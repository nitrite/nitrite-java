package org.dizitart.no2.filters;

import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexScanner;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;

/**
 * @author Anindya Chatterjee
 */
@ToString
class NotEqualsFilter extends ComparableFilter {
    protected NotEqualsFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    public boolean apply(Pair<NitriteId, Document> element) {
        Document document = element.getSecond();
        Object fieldValue = document.get(getField());
        return !deepEquals(fieldValue, getValue());
    }

    public Object applyOnIndex(IndexScanner indexScanner) {
        NavigableMap<Comparable<?>, Object> subMap = new ConcurrentSkipListMap<>();
        List<NitriteId> nitriteIds = new ArrayList<>();

        for (Pair<Comparable<?>, ?> entry : indexScanner.entries()) {
            if (!deepEquals(getValue(), entry.getFirst())) {
                processIndexValue(entry.getSecond(), subMap, nitriteIds);
            }
        }

        if (!subMap.isEmpty()) {
            // if sub-map is populated then filtering on compound index, return sub-map
            return subMap;
        } else {
            // else it is filtering on either single field index,
            // or it is a terminal filter on compound index, return only nitrite-ids
            return nitriteIds;
        }
    }
}
