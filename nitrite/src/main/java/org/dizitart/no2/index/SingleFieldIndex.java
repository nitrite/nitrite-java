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
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.UnknownType;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.filters.ComparableFilter;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;
import static org.dizitart.no2.common.util.Iterables.getElementType;
import static org.dizitart.no2.common.util.ObjectUtils.convertToObjectArray;

/**
 * Represents a nitrite index on a single field.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class SingleFieldIndex implements NitriteIndex {
    @Getter
    private final IndexDescriptor indexDescriptor;
    private final NitriteStore<?> nitriteStore;

    /**
     * Instantiates a new {@link SingleFieldIndex}.
     *
     * @param indexDescriptor the index descriptor
     * @param nitriteStore    the nitrite store
     */
    public SingleFieldIndex(IndexDescriptor indexDescriptor, NitriteStore<?> nitriteStore) {
        this.indexDescriptor = indexDescriptor;
        this.nitriteStore = nitriteStore;
    }

    @Override
    public void write(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        if (element == null) {
            NitriteMap<Comparable<?>, List<?>> indexMap = findIndexMap(UnknownType.class);

            addIndexElement(indexMap, fieldValues, null);
        } else if (element instanceof Comparable) {
            NitriteMap<Comparable<?>, List<?>> indexMap = findIndexMap(element.getClass());

            addIndexElement(indexMap, fieldValues, (Comparable<?>) element);
        } else if (element.getClass().isArray()) {
            Object[] array = convertToObjectArray(element);
            NitriteMap<Comparable<?>, List<?>> indexMap = findIndexMap(array.getClass().getComponentType());

            for (Object item : array) {
                addIndexElement(indexMap, fieldValues, (Comparable<?>) item);
            }
        } else if (element instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) element;
            NitriteMap<Comparable<?>, List<?>> indexMap = findIndexMap(getElementType(iterable));

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
        Object element = fieldValues.get(firstField);

        if (element == null) {
            NitriteMap<Comparable<?>, List<?>> indexMap = findIndexMap(UnknownType.class);

            removeIndexElement(indexMap, fieldValues, null);
        } else if (element instanceof Comparable) {
            NitriteMap<Comparable<?>, List<?>> indexMap = findIndexMap(element.getClass());

            removeIndexElement(indexMap, fieldValues, (Comparable<?>) element);
        } else if (element.getClass().isArray()) {
            Object[] array = convertToObjectArray(element);
            NitriteMap<Comparable<?>, List<?>> indexMap = findIndexMap(array.getClass().getComponentType());

            for (Object item : array) {
                removeIndexElement(indexMap, fieldValues, (Comparable<?>) item);
            }
        } else if (element instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) element;
            NitriteMap<Comparable<?>, List<?>> indexMap = findIndexMap(getElementType(iterable));

            for (Object item : iterable) {
                removeIndexElement(indexMap, fieldValues, (Comparable<?>) item);
            }
        }
    }

    @Override
    public void drop() {
        NitriteMap<Comparable<?>, List<?>> indexMap = findIndexMap(UnknownType.class);
        indexMap.clear();
        indexMap.drop();
    }

    @Override
    public LinkedHashSet<NitriteId> findNitriteIds(FindPlan findPlan) {
        if (findPlan.getIndexScanFilter() == null) return new LinkedHashSet<>();

        NitriteMap<Comparable<?>, List<?>> indexMap = findIndexMap(UnknownType.class);
        return scanIndex(findPlan, indexMap);
    }

    @SuppressWarnings("unchecked")
    private void addIndexElement(NitriteMap<Comparable<?>, List<?>> indexMap,
                                 FieldValues fieldValues, Comparable<?> element) {
        List<NitriteId> nitriteIds = (List<NitriteId>) indexMap.get(element);
        nitriteIds = addNitriteIds(nitriteIds, fieldValues);
        indexMap.put(element, nitriteIds);
    }

    @SuppressWarnings("unchecked")
    private void removeIndexElement(NitriteMap<Comparable<?>, List<?>> indexMap,
                                    FieldValues fieldValues, Comparable<?> element) {
        List<NitriteId> nitriteIds = (List<NitriteId>) indexMap.get(element);
        if (nitriteIds != null && !nitriteIds.isEmpty()) {
            nitriteIds.remove(fieldValues.getNitriteId());
            if (nitriteIds.size() == 0) {
                indexMap.remove(element);
            } else {
                indexMap.put(element, nitriteIds);
            }
        }
    }

    private NitriteMap<Comparable<?>, List<?>> findIndexMap(Class<?> keyType) {
        String mapName = deriveIndexMapName(indexDescriptor);
        return nitriteStore.openMap(mapName, keyType, ArrayList.class);
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<NitriteId> scanIndex(FindPlan findPlan,
                                            NitriteMap<Comparable<?>, List<?>> indexMap) {
        // linked-hash-set to return only unique ids preserving the order in index
        LinkedHashSet<NitriteId> nitriteIds = new LinkedHashSet<>();
        List<Filter> filters = findPlan.getIndexScanFilter().getFilters();

        if (filters != null && filters.size() == 1) {
            Filter filter = filters.get(0);
            if (filter instanceof ComparableFilter) {
                ComparableFilter comparableFilter = (ComparableFilter) filter;
                // filter will return a list of nitrite-ids

                IndexScanner indexScanner = new IndexScanner(indexMap);
                boolean reverseScan = findPlan.getIndexScanOrder().get(comparableFilter.getField());
                indexScanner.setReverseScan(reverseScan);

                Object scanResult = comparableFilter.applyOnIndex(indexScanner);

                if (scanResult instanceof List) {
                    // for list take the nitrite-ids maintaining the insertion order
                    List<NitriteId> terminalResult = (List<NitriteId>) scanResult;
                    nitriteIds.addAll(terminalResult);
                    return nitriteIds;
                }
            }
        }

        throw new FilterException("invalid result state after index scan");
    }
}
