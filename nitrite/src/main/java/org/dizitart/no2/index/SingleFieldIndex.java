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
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.dizitart.no2.common.util.IndexUtils.deriveCompositeIndexMapName;
import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;
import static org.dizitart.no2.common.util.IndexUtils.deriveUniqueIndexMapName;
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
    private volatile boolean uniqueMigrationChecked;

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
     * The composite-key layout (issue #1260) is used for every non-unique index. A unique index
     * has at most one id per key, so it stores that id directly ({@code value -> id}); the
     * classic {@code value -> [id]} list layout it used before is migrated on first use.
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
            // one id per key: a violation is another document already holding the key
            NitriteMap<DBValue, NitriteId> indexMap = findUniqueMap();
            forEachElement(element, dbValue -> {
                NitriteId existing = indexMap.get(dbValue);
                if (existing != null && !existing.equals(fieldValues.getNitriteId())) {
                    throw new UniqueConstraintException("Unique key constraint violation for " + fields);
                }
                indexMap.put(dbValue, fieldValues.getNitriteId());
            });
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
            NitriteMap<DBValue, NitriteId> indexMap = findUniqueMap();
            forEachElement(element, dbValue -> {
                NitriteId existing = indexMap.get(dbValue);
                if (existing != null && existing.equals(fieldValues.getNitriteId())) {
                    indexMap.remove(dbValue);
                }
            });
        } else {
            NitriteMap<IndexEntryKey, Object> indexMap = findCompositeMap();
            forEachElement(element, dbValue ->
                indexMap.remove(new IndexEntryKey(dbValue, fieldValues.getNitriteId())));
        }
    }

    @Override
    public void drop() {
        if (!useCompositeLayout()) {
            // drop whichever layouts exist without migrating first; nothing being dropped
            // needs converting
            dropMapIfPresent(deriveUniqueIndexMapName(indexDescriptor), DBValue.class, NitriteId.class);
            dropMapIfPresent(deriveIndexMapName(indexDescriptor), DBValue.class, ArrayList.class);
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
            : IndexMap.unique(findUniqueMap());
        return scanIndex(findPlan, iMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Pair<DBValue, NitriteId>> readSortKeys(long collectionSize) {
        List<Pair<DBValue, NitriteId>> keys = new ArrayList<>();
        Set<NitriteId> seen = new HashSet<>();

        if (useCompositeLayout()) {
            // the composite key carries both halves, so the values are never read
            for (Pair<IndexEntryKey, Object> entry : findCompositeMap().entries()) {
                IndexEntryKey key = entry.getFirst();
                if (!seen.add(key.getNitriteId())) return null;
                keys.add(new Pair<>(key.getValue(), key.getNitriteId()));
            }
        } else {
            for (Pair<DBValue, NitriteId> entry : findUniqueMap().entries()) {
                if (!seen.add(entry.getSecond())) return null;
                keys.add(new Pair<>(entry.getFirst(), entry.getSecond()));
            }
        }

        // one entry per document, no more and no fewer, or the index cannot stand in for
        // the collection and the caller has to sort the documents themselves
        return keys.size() == collectionSize ? keys : null;
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

    private NitriteMap<DBValue, NitriteId> findUniqueMap() {
        migrateLegacyUniqueIndex();
        return nitriteStore.openMap(deriveUniqueIndexMapName(indexDescriptor), DBValue.class, NitriteId.class);
    }

    /**
     * Rewrites a unique index left in the classic {@code value -> [id]} list layout into the
     * single-id layout the first time the index is accessed, then drops the legacy map. The
     * list layout paid a copy-on-write list per key for a list that never held more than one
     * id. Idempotent and run once per index instance.
     */
    @SuppressWarnings("unchecked")
    private void migrateLegacyUniqueIndex() {
        if (uniqueMigrationChecked) return;
        synchronized (this) {
            if (uniqueMigrationChecked) return;
            String legacyName = deriveIndexMapName(indexDescriptor);
            if (nitriteStore.hasMap(legacyName)) {
                NitriteMap<DBValue, List<?>> legacy = findIndexMap();
                if (!legacy.isEmpty()) {
                    NitriteMap<DBValue, NitriteId> unique = nitriteStore.openMap(
                        deriveUniqueIndexMapName(indexDescriptor), DBValue.class, NitriteId.class);
                    for (Pair<DBValue, List<?>> entry : (Iterable<Pair<DBValue, List<?>>>) (Iterable<?>) legacy.entries()) {
                        List<NitriteId> nitriteIds = (List<NitriteId>) entry.getSecond();
                        if (nitriteIds != null && !nitriteIds.isEmpty()) {
                            unique.put(entry.getFirst(), nitriteIds.get(0));
                        }
                    }
                }
                legacy.clear();
                legacy.drop();
            }
            uniqueMigrationChecked = true;
        }
    }

    private void dropMapIfPresent(String mapName, Class<?> keyType, Class<?> valueType) {
        if (nitriteStore.hasMap(mapName)) {
            NitriteMap<?, ?> map = nitriteStore.openMap(mapName, keyType, valueType);
            map.clear();
            map.drop();
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
