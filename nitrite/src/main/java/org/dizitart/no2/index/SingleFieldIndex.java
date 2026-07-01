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
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.ComparableFilter;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.dizitart.no2.common.util.IndexUtils.deriveCompositeIndexMapName;
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
    private volatile boolean migrationChecked;

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

    /**
     * The composite-key layout (issue #1260) is used for every non-unique index. Unique indexes
     * keep the classic {@code value -> [id]} array layout because the uniqueness check relies on
     * its single-array shape.
     */
    private boolean useCompositeLayout() {
        return !isUnique();
    }

    @Override
    public void write(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        if (!useCompositeLayout()) {
            // unique indexes (and stores without comparable key ordering) keep the classic
            // value -> [id] layout.
            NitriteMap<DBValue, List<?>> indexMap = findIndexMap();
            forEachElement(element, dbValue -> addIndexElement(indexMap, fieldValues, dbValue));
        } else {
            // non-unique indexes use the composite-key layout: one O(log n) point write per
            // (value, id) pair, instead of an O(n) read-modify-write of a shared list (issue #1260)
            NitriteMap<IndexEntryKey, Object> indexMap = findCompositeMap();
            forEachElement(element, dbValue ->
                indexMap.put(new IndexEntryKey(dbValue, fieldValues.getNitriteId()), Boolean.TRUE));
        }
    }

    @Override
    public void remove(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        if (!useCompositeLayout()) {
            NitriteMap<DBValue, List<?>> indexMap = findIndexMap();
            forEachElement(element, dbValue -> removeIndexElement(indexMap, fieldValues, dbValue));
        } else {
            NitriteMap<IndexEntryKey, Object> indexMap = findCompositeMap();
            forEachElement(element, dbValue ->
                indexMap.remove(new IndexEntryKey(dbValue, fieldValues.getNitriteId())));
        }
    }

    @Override
    public void drop() {
        if (!useCompositeLayout()) {
            NitriteMap<DBValue, List<?>> indexMap = findIndexMap();
            indexMap.clear();
            indexMap.drop();
        } else {
            NitriteMap<IndexEntryKey, Object> indexMap = findCompositeMap();
            indexMap.clear();
            indexMap.drop();
            dropLegacyMap();
        }
    }

    @Override
    public LinkedHashSet<NitriteId> findNitriteIds(FindPlan findPlan) {
        if (findPlan.getIndexScanFilter() == null) return new LinkedHashSet<>();

        IndexMap iMap = useCompositeLayout()
            ? IndexMap.composite(findCompositeMap())
            : new IndexMap(findIndexMap());
        return scanIndex(findPlan, iMap);
    }

    /**
     * Invokes {@code action} once per indexed value, wrapping each value in a {@link DBValue}
     * (using {@link DBNull} for nulls) and unwrapping arrays and iterables into their elements.
     */
    private void forEachElement(Object element, java.util.function.Consumer<DBValue> action) {
        if (element == null) {
            action.accept(DBNull.getInstance());
        } else if (element instanceof Comparable) {
            action.accept(new DBValue((Comparable<?>) element));
        } else if (element.getClass().isArray()) {
            for (Object item : convertToObjectArray(element)) {
                action.accept(item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item));
            }
        } else if (element instanceof Iterable) {
            for (Object item : (Iterable<?>) element) {
                action.accept(item == null ? DBNull.getInstance() : new DBValue((Comparable<?>) item));
            }
        }
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
        return nitriteStore.openMap(mapName, DBValue.class, ArrayList.class);
    }

    private NitriteMap<IndexEntryKey, Object> findCompositeMap() {
        migrateLegacyIndex();
        String mapName = deriveCompositeIndexMapName(indexDescriptor);
        return nitriteStore.openMap(mapName, IndexEntryKey.class, Boolean.class);
    }

    /**
     * Rewrites a legacy array-format non-unique index (one growing {@code value -> [ids]} row
     * per value, written by Nitrite &lt; 4.4) into the composite-key layout the first time the
     * index is accessed, then drops the legacy map. Idempotent and run once per index instance.
     */
    @SuppressWarnings("unchecked")
    private void migrateLegacyIndex() {
        if (migrationChecked) return;
        synchronized (this) {
            if (migrationChecked) return;
            String legacyName = deriveIndexMapName(indexDescriptor);
            if (nitriteStore.hasMap(legacyName)) {
                NitriteMap<DBValue, List<?>> legacy = nitriteStore.openMap(legacyName,
                    DBValue.class, ArrayList.class);
                if (!legacy.isEmpty()) {
                    String mapName = deriveCompositeIndexMapName(indexDescriptor);
                    NitriteMap<IndexEntryKey, Object> composite = nitriteStore.openMap(mapName,
                        IndexEntryKey.class, Boolean.class);
                    for (Pair<DBValue, List<?>> entry : (Iterable<Pair<DBValue, List<?>>>) (Iterable<?>) legacy.entries()) {
                        DBValue value = entry.getFirst();
                        for (NitriteId nitriteId : (List<NitriteId>) entry.getSecond()) {
                            composite.put(new IndexEntryKey(value, nitriteId), Boolean.TRUE);
                        }
                    }
                }
                legacy.clear();
                legacy.drop();
            }
            migrationChecked = true;
        }
    }

    private void dropLegacyMap() {
        String legacyName = deriveIndexMapName(indexDescriptor);
        if (nitriteStore.hasMap(legacyName)) {
            NitriteMap<DBValue, List<?>> legacy = nitriteStore.openMap(legacyName,
                DBValue.class, ArrayList.class);
            legacy.clear();
            legacy.drop();
        }
    }

    private LinkedHashSet<NitriteId> scanIndex(FindPlan findPlan, IndexMap iMap) {
        List<ComparableFilter> filters = findPlan.getIndexScanFilter().getFilters();
        IndexScanner indexScanner = new IndexScanner(iMap);
        return indexScanner.doScan(filters, findPlan.getIndexScanOrder());
    }
}
