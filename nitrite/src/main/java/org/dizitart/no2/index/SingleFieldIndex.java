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
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.filters.ComparableFilter;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;
import static org.dizitart.no2.common.util.ObjectUtils.convertToObjectArray;

/**
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

        NitriteMap<DBValue, List<?>> indexMap = findIndexMap();

        if (element == null) {
            addIndexElement(indexMap, fieldValues, DBNull.getInstance());
        } else if (element instanceof Comparable) {
            // wrap around db value
            DBValue dbValue = new DBValue((Comparable<?>) element);
            addIndexElement(indexMap, fieldValues, dbValue);
        } else if (element.getClass().isArray()) {
            Object[] array = convertToObjectArray(element);

            for (Object item : array) {
                // wrap around db value
                DBValue dbValue = item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item);
                addIndexElement(indexMap, fieldValues, dbValue);
            }
        } else if (element instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) element;

            for (Object item : iterable) {
                // wrap around db value
                DBValue dbValue = item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item);
                addIndexElement(indexMap, fieldValues, dbValue);
            }
        }
    }

    @Override
    public void remove(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        NitriteMap<DBValue, List<?>> indexMap = findIndexMap();
        if (element == null) {
            removeIndexElement(indexMap, fieldValues, DBNull.getInstance());
        } else if (element instanceof Comparable) {
            // wrap around db value
            DBValue dbValue = new DBValue((Comparable<?>) element);
            removeIndexElement(indexMap, fieldValues, dbValue);
        } else if (element.getClass().isArray()) {
            Object[] array = convertToObjectArray(element);

            for (Object item : array) {
                // wrap around db value
                DBValue dbValue = item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item);
                removeIndexElement(indexMap, fieldValues, dbValue);
            }
        } else if (element instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) element;

            for (Object item : iterable) {
                // wrap around db value
                DBValue dbValue = item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item);
                removeIndexElement(indexMap, fieldValues, dbValue);
            }
        }
    }

    @Override
    public void drop() {
        NitriteMap<DBValue, List<?>> indexMap = findIndexMap();
        indexMap.clear();
        indexMap.drop();
    }

    @Override
    public LinkedHashSet<NitriteId> findNitriteIds(FindPlan findPlan) {
        if (findPlan.getIndexScanFilter() == null) return new LinkedHashSet<>();

        NitriteMap<DBValue, List<?>> indexMap = findIndexMap();
        return scanIndex(findPlan, indexMap);
    }

    @SuppressWarnings("unchecked")
    private void addIndexElement(NitriteMap<DBValue, List<?>> indexMap,
                                 FieldValues fieldValues, DBValue element) {
        List<NitriteId> nitriteIds = (List<NitriteId>) indexMap.get(element);
        nitriteIds = addNitriteIds(nitriteIds, fieldValues);
        indexMap.put(element, nitriteIds);
    }

    @SuppressWarnings("unchecked")
    private void removeIndexElement(NitriteMap<DBValue, List<?>> indexMap,
                                    FieldValues fieldValues, DBValue element) {
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

    private NitriteMap<DBValue, List<?>> findIndexMap() {
        String mapName = deriveIndexMapName(indexDescriptor);
        return nitriteStore.openMap(mapName, DBValue.class, CopyOnWriteArrayList.class);
    }

    private LinkedHashSet<NitriteId> scanIndex(FindPlan findPlan,
                                            NitriteMap<DBValue, List<?>> indexMap) {
        List<ComparableFilter> filters = findPlan.getIndexScanFilter().getFilters();
        IndexMap iMap = new IndexMap(indexMap);
        IndexScanner indexScanner = new IndexScanner(iMap);
        return indexScanner.doScan(filters, findPlan.getIndexScanOrder());
    }
}
