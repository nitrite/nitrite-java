/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.index;

import lombok.Getter;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.*;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.filters.ComparableFilter;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;
import static org.dizitart.no2.common.util.Iterables.getElementType;
import static org.dizitart.no2.common.util.ObjectUtils.convertToObjectArray;

/**
 * Represents a nitrite compound index.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class CompoundIndex implements NitriteIndex {
    @Getter
    private final IndexDescriptor indexDescriptor;
    private final NitriteStore<?> nitriteStore;

    /**
     * Instantiates a new Compound index.
     *
     * @param indexDescriptor the index descriptor
     * @param nitriteStore    the nitrite store
     */
    public CompoundIndex(IndexDescriptor indexDescriptor, NitriteStore<?> nitriteStore) {
        this.indexDescriptor = indexDescriptor;
        this.nitriteStore = nitriteStore;
    }

    public void write(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object firstValue = fieldValues.get(firstField);

        // NOTE: only first field can have array or iterable value, subsequent fields can not
        validateIndexField(firstValue, firstField);

        if (firstValue == null) {
            NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap = findIndexMap(UnknownType.class);

            addIndexElement(indexMap, fieldValues, null);
        } else if (firstValue instanceof Comparable) {
            NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap = findIndexMap(firstValue.getClass());

            addIndexElement(indexMap, fieldValues, (Comparable<?>) firstValue);
        } else if (firstValue.getClass().isArray()) {
            Object[] array = convertToObjectArray(firstValue);
            NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap = findIndexMap(array.getClass().getComponentType());

            for (Object item : array) {
                addIndexElement(indexMap, fieldValues, (Comparable<?>) item);
            }
        } else if (firstValue instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) firstValue;
            NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap = findIndexMap(getElementType(iterable));

            for (Object item : iterable) {
                addIndexElement(indexMap, fieldValues, (Comparable<?>) item);
            }
        }
    }

    @Override
    public void remove(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object firstValue = fieldValues.get(firstField);

        // NOTE: only first field can have array or iterable value, subsequent fields can not
        validateIndexField(firstValue, firstField);

        if (firstValue == null) {
            NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap = findIndexMap(UnknownType.class);

            removeIndexElement(indexMap, fieldValues, null);
        } else if (firstValue instanceof Comparable) {
            NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap = findIndexMap(firstValue.getClass());

            removeIndexElement(indexMap, fieldValues, (Comparable<?>) firstValue);
        } else if (firstValue.getClass().isArray()) {
            Object[] array = convertToObjectArray(firstValue);
            NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap = findIndexMap(array.getClass().getComponentType());

            for (Object item : array) {
                removeIndexElement(indexMap, fieldValues, (Comparable<?>) item);
            }
        } else if (firstValue instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) firstValue;
            NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap = findIndexMap(getElementType(iterable));

            for (Object item : iterable) {
                removeIndexElement(indexMap, fieldValues, (Comparable<?>) item);
            }
        }
    }

    @Override
    public void drop() {
        NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap = findIndexMap(UnknownType.class);
        indexMap.clear();
        indexMap.drop();
    }

    @Override
    public LinkedHashSet<NitriteId> findNitriteIds(FindPlan findPlan) {
        if (findPlan.getIndexScanFilter() == null) return new LinkedHashSet<>();

        NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap = findIndexMap(UnknownType.class);
        return scanIndex(findPlan, indexMap);
    }

    private void addIndexElement(NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap,
                                 FieldValues fieldValues, Comparable<?> element) {
        NavigableMap<?, ?> subMap = indexMap.get(element);
        if (subMap == null) {
            // index are always in ascending order
            subMap = new ConcurrentSkipListMap<>();
        }

        populateSubMap(subMap, fieldValues, 1);
        indexMap.put(element, subMap);
    }

    private void removeIndexElement(NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap,
                                    FieldValues fieldValues, Comparable<?> element) {
        NavigableMap<?, ?> subMap = indexMap.get(element);
        if (subMap != null && !subMap.isEmpty()) {
            deleteFromSubMap(subMap, fieldValues, 1);
            indexMap.put(element, subMap);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void populateSubMap(NavigableMap subMap, FieldValues fieldValues, int startIndex) {
        for (int i = startIndex; i < fieldValues.getValues().size(); i++) {
            Pair<String, Object> pair = fieldValues.getValues().get(i);
            Object value = pair.getSecond();
            if (value == null) {
                value = DBNull.getInstance();
            }

            if (!(value instanceof Comparable)) {
                throw new IndexingException(value + " is not comparable");
            }

            if (i == fieldValues.getValues().size() - 1) {
                List<NitriteId> nitriteIds = (List<NitriteId>) subMap.get(value);
                nitriteIds = addNitriteIds(nitriteIds, fieldValues);
                subMap.put(value, nitriteIds);
            } else {
                NavigableMap subMap2 = (NavigableMap) subMap.get(value);
                if (subMap2 == null) {
                    // index are always in ascending order
                    subMap2 = new ConcurrentSkipListMap<>();
                }

                populateSubMap(subMap2, fieldValues, startIndex + 1);
                subMap.put(value, subMap2);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void deleteFromSubMap(NavigableMap subMap, FieldValues fieldValues, int startIndex) {
        for (int i = startIndex; i < fieldValues.getValues().size(); i++) {
            Pair<String, Object> pair = fieldValues.getValues().get(i);
            Object value = pair.getSecond();
            if (value == null) {
                value = DBNull.getInstance();
            }

            if (!(value instanceof Comparable)) {
                continue;
            }

            if (i == fieldValues.getValues().size() - 1) {
                List<NitriteId> nitriteIds = (List<NitriteId>) subMap.get(value);
                nitriteIds = removeNitriteIds(nitriteIds, fieldValues);
                if (nitriteIds == null || nitriteIds.isEmpty()) {
                    subMap.remove(value);
                } else {
                    subMap.put(value, nitriteIds);
                }
            } else {
                NavigableMap subMap2 = (NavigableMap) subMap.get(value);
                if (subMap2 == null) {
                    continue;
                }

                deleteFromSubMap(subMap2, fieldValues, startIndex + 1);
                subMap.put(value, subMap2);
            }
        }
    }

    private NitriteMap<Comparable<?>, NavigableMap<?, ?>> findIndexMap(Class<?> keyType) {
        String mapName = deriveIndexMapName(indexDescriptor);
        return nitriteStore.openMap(mapName, keyType, ConcurrentSkipListMap.class);
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<NitriteId> scanIndex(FindPlan findPlan,
                                               NitriteMap<Comparable<?>, NavigableMap<?, ?>> indexMap) {
        // linked-hash-set to return only unique ids preserving the order in index
        LinkedHashSet<NitriteId> nitriteIds = new LinkedHashSet<>();
        List<Filter> filters = findPlan.getIndexScanFilter().getFilters();

        if (filters != null && !filters.isEmpty()) {
            Object scanResult = indexMap;
            for (Filter filter : filters) {
                if (scanResult == null) {
                    // invalid scanning state
                    break;
                }

                if (filter instanceof ComparableFilter) {
                    ComparableFilter comparableFilter = (ComparableFilter) filter;
                    // filter can consume nitrite-map or sub-map
                    // filter can return list of nitrite-id or sub-map

                    IndexScanner indexScanner = null;
                    boolean reverseScan = findPlan.getIndexScanOrder().get(comparableFilter.getField());
                    if (scanResult instanceof NitriteMap) {
                        indexScanner = new IndexScanner((NitriteMap<Comparable<?>, ?>) scanResult);
                    } else if (scanResult instanceof NavigableMap) {
                        indexScanner = new IndexScanner((NavigableMap<Comparable<?>, ?>) scanResult);
                    }

                    if (indexScanner != null) {
                        indexScanner.setReverseScan(reverseScan);
                        scanResult = comparableFilter.applyOnIndex(indexScanner);
                    }
                } else {
                    throw new FilterException("index scan is not supported for " + filter.getClass().getName());
                }
            }

            // final result of last filter can be a navigable map or list
            if (scanResult instanceof List) {
                // for linked list take the nitrite-ids maintaining the insertion order
                List<NitriteId> terminalResult = (List<NitriteId>) scanResult;
                nitriteIds.addAll(terminalResult);
            } else if (scanResult instanceof NavigableMap) {
                // if filter is a covered query but index still have some extra field,
                // then the return type of last filter will be a navigable-map
                // we need to traverse the rest of the data and collect all
                // terminal nitrite-ids preserving the insertion order
                NavigableMap<?, ?> subMap = (NavigableMap<?, ?>) scanResult;

                // collection all terminal nitrite-ids
                List<NitriteId> terminalResult = getTerminalNitriteIds(subMap);
                nitriteIds.addAll(terminalResult);
            } else {
                throw new FilterException("invalid result state after index scan");
            }
        }
        return nitriteIds;
    }


    @SuppressWarnings("unchecked")
    private List<NitriteId> getTerminalNitriteIds(NavigableMap<?, ?> navigableMap) {
        List<NitriteId> terminalResult = new ArrayList<>();

        // scan each entry of th navigable map and collect all terminal nitrite-ids
        for (Map.Entry<?, ?> entry : navigableMap.entrySet()) {
            // if the value is terminal, collect all nitrite-ids
            if (entry.getValue() instanceof List) {
                List<NitriteId> nitriteIds = (List<NitriteId>) entry.getValue();
                terminalResult.addAll(nitriteIds);
            }

            // if the value is not terminal, scan recursively
            if (entry.getValue() instanceof NavigableMap) {
                NavigableMap<?, ?> subMap = (NavigableMap<?, ?>) entry.getValue();
                List<NitriteId> nitriteIds = getTerminalNitriteIds(subMap);
                terminalResult.addAll(nitriteIds);
            }
        }
        return terminalResult;
    }
}
