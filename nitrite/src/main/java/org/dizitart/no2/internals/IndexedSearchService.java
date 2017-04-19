package org.dizitart.no2.internals;

import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.fulltext.TextIndexingService;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.dizitart.no2.exceptions.ErrorMessage.CAN_NOT_SEARCH_NON_COMPARABLE_ON_INDEXED_FIELD;

/**
 * @author Anindya Chatterjee.
 */
class IndexedSearchService {
    private IndexMetaService indexMetaService;
    private TextIndexingService textIndexingService;

    IndexedSearchService(IndexMetaService indexMetaService, TextIndexingService textIndexingService) {
        this.indexMetaService = indexMetaService;
        this.textIndexingService = textIndexingService;
    }

    Set<NitriteId> findEqual(String field, Object value) {
        if (!(value instanceof Comparable)) {
            throw new FilterException(CAN_NOT_SEARCH_NON_COMPARABLE_ON_INDEXED_FIELD);
        }

        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexMetaService.getIndexMap(field);

        Set<NitriteId> resultSet = null;
        if (indexMap != null) {
            resultSet = indexMap.get((Comparable) value);
        }

        if (resultSet == null) resultSet = new LinkedHashSet<>();
        return resultSet;
    }

    Set<NitriteId> findGreaterThan(String field, Comparable comparable) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexMetaService.getIndexMap(field);

        if (indexMap != null) {
            Comparable higherKey = indexMap.higherKey(comparable);
            while (higherKey != null) {
                resultSet.addAll(indexMap.get(higherKey));
                higherKey = indexMap.higherKey(higherKey);
            }
        }

        return resultSet;
    }

    Set<NitriteId> findGreaterEqual(String field, Comparable comparable) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexMetaService.getIndexMap(field);

        if (indexMap != null) {
            Comparable ceilingKey = indexMap.ceilingKey(comparable);
            while (ceilingKey != null) {
                resultSet.addAll(indexMap.get(ceilingKey));
                ceilingKey = indexMap.higherKey(ceilingKey);
            }
        }

        return resultSet;
    }

    Set<NitriteId> findLesserThan(String field, Comparable comparable) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexMetaService.getIndexMap(field);

        if (indexMap != null) {
            Comparable lowerKey = indexMap.lowerKey(comparable);
            while (lowerKey != null) {
                resultSet.addAll(indexMap.get(lowerKey));
                lowerKey = indexMap.lowerKey(lowerKey);
            }
        }

        return resultSet;
    }

    Set<NitriteId> findLesserEqual(String field, Comparable comparable) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexMetaService.getIndexMap(field);

        if (indexMap != null) {
            Comparable floorKey = indexMap.floorKey(comparable);
            while (floorKey != null) {
                resultSet.addAll(indexMap.get(floorKey));
                floorKey = indexMap.lowerKey(floorKey);
            }
        }

        return resultSet;
    }

    Set<NitriteId> findIn(String field, List<Object> values) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexMetaService.getIndexMap(field);

        if (indexMap != null) {
            for (Comparable comparable : indexMap.keySet()) {
                if (values.contains(comparable)) {
                    resultSet.addAll(indexMap.get(comparable));
                }
            }
        }

        return resultSet;
    }

    Set<NitriteId> findText(String field, String value) {
        return textIndexingService.searchByIndex(field, value);
    }
}
