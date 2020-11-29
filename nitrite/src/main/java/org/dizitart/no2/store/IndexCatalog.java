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

package org.dizitart.no2.store;

import org.dizitart.no2.common.FieldNames;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.IndexedFieldNames;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.Constants.*;

/**
 *
 * @since 4.0
 * @author Anindya Chatterjee
 */
public class IndexCatalog {
    private final NitriteStore<?> nitriteStore;

    public IndexCatalog(NitriteStore<?> nitriteStore) {
        this.nitriteStore = nitriteStore;
    }

    public boolean hasIndexDescriptor(String collectionName, Fields fields) {
        NitriteMap<Fields, IndexMeta> indexMetaMap = getIndexMetaMap(collectionName);
        if (!indexMetaMap.containsKey(fields)) return false;

        IndexMeta indexMeta = indexMetaMap.get(fields);
        return indexMeta != null;
    }

    public IndexDescriptor createIndexDescriptor(String collectionName, Fields fields, String indexType) {
        IndexDescriptor index = new IndexDescriptor(indexType, fields, collectionName);

        IndexMeta indexMeta = new IndexMeta();
        indexMeta.setIndexDescriptor(index);
        indexMeta.setIsDirty(new AtomicBoolean(false));
        indexMeta.setIndexMap(getIndexMapName(index));

        getIndexMetaMap(collectionName).put(fields, indexMeta);

        return index;
    }

    public IndexDescriptor findIndexDescriptorExact(String collectionName, Fields fields) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(fields);
        if (meta != null) {
            return meta.getIndexDescriptor();
        }
        return null;
    }

    public Set<IndexedFieldNames> findIndexSupportedFields(String collectionName) {
        Collection<IndexDescriptor> indexDescriptors = listIndexDescriptors(collectionName);
        Map<FieldNames, Set<IndexDescriptor>> fieldIndexMap = new ConcurrentHashMap<>();

        // get actual index descriptors
        for (IndexDescriptor indexDescriptor : indexDescriptors) {
            // create all possible combinations of fields in the same order
            // if the index field is [a,b,c] then combinations would be
            // like - [[a], [a,b], [a,b,c]]
            List<FieldNames> fieldsCombinations = new ArrayList<>();
            Fields indexFields = indexDescriptor.getIndexFields();

            // create combinations of fields
            for (Pair<String, SortOrder> pair : indexFields.getSortSpecs()) {
                FieldNames fn = new FieldNames();
                if (!fieldsCombinations.isEmpty()) {
                    // get the last field combination and add the current field from the pair
                    // [a,b] + c -> [a,b,c]
                    FieldNames lastFields = fieldsCombinations.get(fieldsCombinations.size() - 1);
                    fn.getNames().addAll(lastFields.getNames());
                }
                fn.getNames().add(pair.getFirst());
                fieldsCombinations.add(fn);
            }

            // for each combination, create a map with the index
            // so, if the Ix1 is on fields [a,b,c], then cache would
            // be like - [a] -> Ix1, [a,b] -> Ix1, [a,b,c] -> Ix1
            for (FieldNames fields : fieldsCombinations) {
                Set<IndexDescriptor> descriptorList = fieldIndexMap.get(fields);
                if (descriptorList == null) {
                    descriptorList = new HashSet<>();
                }
                descriptorList.add(indexDescriptor);
                fieldIndexMap.put(fields, descriptorList);
            }
        }

        Set<IndexedFieldNames> resultSet = new HashSet<>();
        for (Map.Entry<FieldNames, Set<IndexDescriptor>> entry : fieldIndexMap.entrySet()) {
            IndexedFieldNames fieldNames = new IndexedFieldNames();
            fieldNames.setNames(entry.getKey().getNames());
            fieldNames.setSupportedIndices(entry.getValue());
            resultSet.add(fieldNames);
        }

        return resultSet;
    }

    public boolean isDirtyIndex(String collectionName, Fields fields) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(fields);
        return meta != null && meta.getIsDirty().get();
    }

    public Collection<IndexDescriptor> listIndexDescriptors(String collectionName) {
        Set<IndexDescriptor> indexSet = new LinkedHashSet<>();
        for (IndexMeta indexMeta : getIndexMetaMap(collectionName).values()) {
            indexSet.add(indexMeta.getIndexDescriptor());
        }
        return Collections.unmodifiableSet(indexSet);
    }

    public void dropIndexDescriptor(String collectionName, Fields fields) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(fields);
        if (meta != null && meta.getIndexDescriptor() != null) {
            String indexMapName = meta.getIndexMap();
            nitriteStore.openMap(indexMapName, Object.class, Object.class).drop();
        }
        getIndexMetaMap(collectionName).remove(fields);
    }

    public void beginIndexing(String collectionName, Fields fields) {
        markDirty(collectionName, fields, true);
    }

    public void endIndexing(String collectionName, Fields fields) {
        markDirty(collectionName, fields, false);
    }

    public String getIndexMapName(IndexDescriptor descriptor) {
        return INDEX_PREFIX +
            INTERNAL_NAME_SEPARATOR +
            descriptor.getCollectionName() +
            INTERNAL_NAME_SEPARATOR +
            descriptor.getIndexFields().getEncodedName() +
            INTERNAL_NAME_SEPARATOR +
            descriptor.getIndexType();
    }

    private NitriteMap<Fields, IndexMeta> getIndexMetaMap(String collectionName) {
        String indexMetaName = getIndexMetaName(collectionName);
        return nitriteStore.openMap(indexMetaName, Fields.class, IndexMeta.class);
    }

    private String getIndexMetaName(String collectionName) {
        return INDEX_META_PREFIX + INTERNAL_NAME_SEPARATOR + collectionName;
    }

    private void markDirty(String collectionName, Fields fields, boolean dirty) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(fields);
        if (meta != null && meta.getIndexDescriptor() != null) {
            meta.getIsDirty().set(dirty);
        }
    }
}
