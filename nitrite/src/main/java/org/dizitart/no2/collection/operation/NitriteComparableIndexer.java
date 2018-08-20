/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.store.IndexStore;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.CAN_NOT_SEARCH_NON_COMPARABLE_ON_INDEXED_FIELD;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.dizitart.no2.util.ValidationUtils.notNull;
import static org.dizitart.no2.util.ValidationUtils.validateDocumentIndexField;

/**
 * @author Anindya Chatterjee
 */
class NitriteComparableIndexer implements ComparableIndexer {
    private IndexStore indexStore;
    private NitriteMap<NitriteId, Document> underlyingMap;
    private final Object lock = new Object();

    NitriteComparableIndexer(
            NitriteMap<NitriteId, Document> underlyingMap,
            IndexStore indexStore) {
        this.indexStore = indexStore;
        this.underlyingMap = underlyingMap;
    }

    @Override
    public void writeIndex(NitriteId id, String field, Comparable element, boolean unique) {
        synchronized (lock) {
            NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                    = indexStore.getIndexMap(field);

            // create the nitriteId list associated with the value
            ConcurrentSkipListSet<NitriteId> nitriteIdList
                    = indexMap.get(element);

            if (nitriteIdList == null) {
                nitriteIdList = new ConcurrentSkipListSet<>();
            }

            if (unique && nitriteIdList.size() == 1
                    && !nitriteIdList.contains(id)) {
                // if key is already exists for unique type, throw error
                throw new UniqueConstraintException(errorMessage(
                        "unique key constraint violation for " + field,
                        UCE_WRITE_INDEX_CONSTRAINT_VIOLATED));
            }

            nitriteIdList.add(id);
            indexMap.put(element, nitriteIdList);
        }
    }

    @Override
    public void updateIndex(NitriteId id, String field, Comparable newElement, Comparable oldElement, boolean unique) {
        synchronized (lock) {
            NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                    = indexStore.getIndexMap(field);

            // create the nitriteId list associated with the value
            ConcurrentSkipListSet<NitriteId> nitriteIdList
                    = indexMap.get(newElement);

            if (nitriteIdList == null) {
                nitriteIdList = new ConcurrentSkipListSet<>();
            }

            if (unique && nitriteIdList.size() == 1
                    && !nitriteIdList.contains(id)) {
                // if key is already exists for unique type, throw error
                throw new UniqueConstraintException(errorMessage(
                        "unique key constraint violation for " + field,
                        UCE_UPDATE_INDEX_CONSTRAINT_VIOLATED));
            }

            // add the nitriteId to the list
            nitriteIdList.add(id);
            indexMap.put(newElement, nitriteIdList);

            nitriteIdList = indexMap.get(oldElement);
            if (nitriteIdList != null && !nitriteIdList.isEmpty()) {
                nitriteIdList.remove(id);
                if (nitriteIdList.size() == 0) {
                    indexMap.remove(oldElement);
                } else {
                    indexMap.put(oldElement, nitriteIdList);
                }
            }
        }
    }

    @Override
    public void removeIndex(NitriteId id, String field, Comparable element) {
        synchronized (lock) {
            NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                    = indexStore.getIndexMap(field);

            // create the nitrite list associated with the value
            ConcurrentSkipListSet<NitriteId> nitriteIdList = indexMap.get(element);
            if (nitriteIdList != null) {
                nitriteIdList.remove(id);
                if (nitriteIdList.size() == 0) {
                    indexMap.remove(element);
                } else {
                    indexMap.put(element, nitriteIdList);
                }
            }
        }
    }

    @Override
    public void dropIndex(String field) {
        synchronized (lock) {
            indexStore.dropIndex(field);
        }
    }

    @Override
    public void rebuildIndex(String field, boolean unique) {
        // create index map
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);

        // remove old values
        indexMap.clear();

        for (Map.Entry<NitriteId, Document> entry : underlyingMap.entrySet()) {
            // create the document
            Document object = entry.getValue();

            // retrieved the value from document
            Object fieldValue = getFieldValue(object, field);

            if (fieldValue == null) continue;
            validateDocumentIndexField(fieldValue, field);

            // create the id list associated with the value
            ConcurrentSkipListSet<NitriteId> nitriteIdList = indexMap.get((Comparable) fieldValue);
            if (nitriteIdList == null) {
                nitriteIdList = new ConcurrentSkipListSet<>();
            }

            if (unique && nitriteIdList.size() == 1) {
                // if key is already exists for unique type, throw error
                throw new UniqueConstraintException(errorMessage(
                        "unique key constraint violation for " + field,
                        UCE_BUILD_INDEX_CONSTRAINT_VIOLATED));
            }

            // add the id to the list
            nitriteIdList.add(entry.getKey());
            indexMap.put((Comparable) fieldValue, nitriteIdList);
        }
    }

    @Override
    public Set<NitriteId> findEqual(String field, Object value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_EQUAL_INDEX_NULL_FIELD));
        if (value == null) return new HashSet<>();

        if (!(value instanceof Comparable)) {
            throw new FilterException(CAN_NOT_SEARCH_NON_COMPARABLE_ON_INDEXED_FIELD);
        }

        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);

        Set<NitriteId> resultSet = null;
        if (indexMap != null) {
            resultSet = indexMap.get((Comparable) value);
        }

        if (resultSet == null) resultSet = new LinkedHashSet<>();
        return resultSet;
    }

    @Override
    public Set<NitriteId> findGreaterThan(String field, Comparable comparable) {
        notNull(field, errorMessage("field can not be null", VE_FIND_GT_INDEX_NULL_FIELD));
        notNull(comparable, errorMessage("comparable can not be null", VE_FIND_GT_INDEX_NULL_VALUE));

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);

        if (indexMap != null) {
            Comparable higherKey = indexMap.higherKey(comparable);
            while (higherKey != null) {
                resultSet.addAll(indexMap.get(higherKey));
                higherKey = indexMap.higherKey(higherKey);
            }
        }

        return resultSet;
    }

    @Override
    public Set<NitriteId> findGreaterEqual(String field, Comparable comparable) {
        notNull(field, errorMessage("field can not be null", VE_FIND_GTE_INDEX_NULL_FIELD));
        notNull(comparable, errorMessage("comparable can not be null", VE_FIND_GTE_INDEX_NULL_VALUE));

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);

        if (indexMap != null) {
            Comparable ceilingKey = indexMap.ceilingKey(comparable);
            while (ceilingKey != null) {
                resultSet.addAll(indexMap.get(ceilingKey));
                ceilingKey = indexMap.higherKey(ceilingKey);
            }
        }

        return resultSet;
    }

    @Override
    public Set<NitriteId> findLesserThan(String field, Comparable comparable) {
        notNull(field, errorMessage("field can not be null", VE_FIND_LT_INDEX_NULL_FIELD));
        notNull(comparable, errorMessage("comparable can not be null", VE_FIND_LT_INDEX_NULL_VALUE));

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);

        if (indexMap != null) {
            Comparable lowerKey = indexMap.lowerKey(comparable);
            while (lowerKey != null) {
                resultSet.addAll(indexMap.get(lowerKey));
                lowerKey = indexMap.lowerKey(lowerKey);
            }
        }

        return resultSet;
    }

    @Override
    public Set<NitriteId> findLesserEqual(String field, Comparable comparable) {
        notNull(field, errorMessage("field can not be null", VE_FIND_LTE_INDEX_NULL_FIELD));
        notNull(comparable, errorMessage("comparable can not be null", VE_FIND_LTE_INDEX_NULL_VALUE));

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);

        if (indexMap != null) {
            Comparable floorKey = indexMap.floorKey(comparable);
            while (floorKey != null) {
                resultSet.addAll(indexMap.get(floorKey));
                floorKey = indexMap.lowerKey(floorKey);
            }
        }

        return resultSet;
    }

    @Override
    public Set<NitriteId> findIn(String field, List<Object> values) {
        notNull(field, errorMessage("field can not be null", VE_FIND_IN_INDEX_NULL_FIELD));
        notNull(values, errorMessage("values can not be null", VE_FIND_IN_INDEX_NULL_VALUE));

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                = indexStore.getIndexMap(field);

        if (indexMap != null) {
            for (Comparable comparable : indexMap.keySet()) {
                if (values.contains(comparable)) {
                    resultSet.addAll(indexMap.get(comparable));
                }
            }
        }

        return resultSet;
    }
}
