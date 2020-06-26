/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.index;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.dizitart.no2.common.util.ObjectUtils.convertToObjectArray;
import static org.dizitart.no2.common.util.ValidationUtils.*;

/**
 * @author Anindya Chatterjee
 */
@SuppressWarnings("rawtypes")
public abstract class ComparableIndexer implements Indexer {
    private NitriteStore nitriteStore;

    abstract boolean isUnique();

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.nitriteStore = nitriteConfig.getNitriteStore();
    }

    @Override
    public void writeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {
        validateIndexField(fieldValue, field);
        addIndexEntry(collection.getName(), nitriteId, field, fieldValue);
    }

    @Override
    public void removeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {
        validateIndexField(fieldValue, field);
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collection.getName(), field);

        if (fieldValue == null) {
            removeElementFromIndexMap(indexMap, nitriteId, field, null);
        } else if (fieldValue instanceof Comparable) {
            removeElementFromIndexMap(indexMap, nitriteId, field, (Comparable) fieldValue);
        } else if (fieldValue.getClass().isArray()) {
            Object[] array = convertToObjectArray(fieldValue);
            for (Object item : array) {
                removeElementFromIndexMap(indexMap, nitriteId, field, (Comparable) item);
            }
        } else if (fieldValue instanceof Iterable) {
            Iterable iterable = (Iterable) fieldValue;
            for (Object item : iterable) {
                removeElementFromIndexMap(indexMap, nitriteId, field, (Comparable) item);
            }
        }
    }

    @Override
    public void updateIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object newValue, Object oldValue) {
        validateIndexField(newValue, field);
        validateIndexField(oldValue, field);
        addIndexEntry(collection.getName(), nitriteId, field, newValue);
        removeIndex(collection, nitriteId, field, oldValue);
    }

    @Override
    public void dropIndex(NitriteMap<NitriteId, Document> collection, String field) {
        // no action required
    }

    public Set<NitriteId> findEqual(String collectionName, String field, Comparable value) {
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        Set<NitriteId> resultSet = null;
        if (indexMap != null) {
            resultSet = indexMap.get(value);
        }

        if (resultSet == null) resultSet = new LinkedHashSet<>();
        return resultSet;
    }

    public Set<NitriteId> findGreaterThan(String collectionName, String field, Comparable comparable) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            Comparable higherKey = indexMap.higherKey(comparable);
            while (higherKey != null) {
                resultSet.addAll(indexMap.get(higherKey));
                higherKey = indexMap.higherKey(higherKey);
            }
        }

        return resultSet;
    }

    public Set<NitriteId> findGreaterEqual(String collectionName, String field, Comparable comparable) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            Comparable ceilingKey = indexMap.ceilingKey(comparable);
            while (ceilingKey != null) {
                resultSet.addAll(indexMap.get(ceilingKey));
                ceilingKey = indexMap.higherKey(ceilingKey);
            }
        }

        return resultSet;
    }

    public Set<NitriteId> findLesserThan(String collectionName, String field, Comparable comparable) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            Comparable lowerKey = indexMap.lowerKey(comparable);
            while (lowerKey != null) {
                resultSet.addAll(indexMap.get(lowerKey));
                lowerKey = indexMap.lowerKey(lowerKey);
            }
        }

        return resultSet;
    }

    public Set<NitriteId> findLesserEqual(String collectionName, String field, Comparable comparable) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            Comparable floorKey = indexMap.floorKey(comparable);
            while (floorKey != null) {
                resultSet.addAll(indexMap.get(floorKey));
                floorKey = indexMap.lowerKey(floorKey);
            }
        }

        return resultSet;
    }

    public Set<NitriteId> findIn(String collectionName, String field, Collection<Comparable> values) {
        notNull(values, "values cannot be null");

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            for (Comparable comparable : indexMap.keySet()) {
                if (values.contains(comparable)) {
                    resultSet.addAll(indexMap.get(comparable));
                }
            }
        }

        return resultSet;
    }

    public Set<NitriteId> findNotIn(String collectionName, String field, Collection<Comparable> values) {
        notNull(values, "values cannot be null");

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            for (Comparable comparable : indexMap.keySet()) {
                if (!values.contains(comparable)) {
                    resultSet.addAll(indexMap.get(comparable));
                }
            }
        }

        return resultSet;
    }

    private void validateIndexField(Object value, String field) {
        if (value == null) return;
        if (value instanceof Iterable) {
            validateIterableIndexField((Iterable) value, field);
        } else if (value.getClass().isArray()) {
            validateArrayIndexField(value, field);
        } else {
            if (!(value instanceof Comparable)) {
                throw new ValidationException(value + " is not comparable");
            }
        }
    }

    private void addIndexEntry(String collectionName, NitriteId id, String field, Object element) {
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (element == null) {
            addElementToIndexMap(indexMap, id, field, null);
        } else if (element instanceof Comparable) {
            addElementToIndexMap(indexMap, id, field, (Comparable) element);
        } else if (element.getClass().isArray()) {
            Object[] array = convertToObjectArray(element);
            for (Object item : array) {
                addElementToIndexMap(indexMap, id, field, (Comparable) item);
            }
        } else if (element instanceof Iterable) {
            Iterable iterable = (Iterable) element;
            for (Object item : iterable) {
                addElementToIndexMap(indexMap, id, field, (Comparable) item);
            }
        }
    }

    private void addElementToIndexMap(NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap,
                                      NitriteId id, String field, Comparable element) {
        // create the nitriteId list associated with the value
        ConcurrentSkipListSet<NitriteId> nitriteIdList
            = indexMap.get(element);

        if (nitriteIdList == null) {
            nitriteIdList = new ConcurrentSkipListSet<>();
        }

        if (isUnique() && nitriteIdList.size() == 1
            && !nitriteIdList.contains(id)) {
            // if key is already exists for unique type, throw error
            throw new UniqueConstraintException("unique key constraint violation for " + field);
        }

        nitriteIdList.add(id);
        indexMap.put(element, nitriteIdList);
    }

    private void removeElementFromIndexMap(NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap,
                                           NitriteId nitriteId, String field, Comparable element) {
        // create the nitrite list associated with the value
        ConcurrentSkipListSet<NitriteId> nitriteIdList = indexMap.get(element);
        if (nitriteIdList != null && !nitriteIdList.isEmpty()) {
            nitriteIdList.remove(nitriteId);
            if (nitriteIdList.size() == 0) {
                indexMap.remove(element);
            } else {
                indexMap.put(element, nitriteIdList);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> getIndexMap(String collectionName, String field) {
        String mapName = getIndexMapName(collectionName, field);
        return nitriteStore.openMap(mapName);
    }
}
