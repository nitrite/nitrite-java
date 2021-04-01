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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexMeta;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;
import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMetaMapName;

/**
 * Represents the index manager for a collection.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class IndexManager implements AutoCloseable {
    private final NitriteConfig nitriteConfig;
    private final NitriteStore<?> nitriteStore;
    private final String collectionName;
    private final NitriteMap<Fields, IndexMeta> indexMetaMap;
    private Collection<IndexDescriptor> indexDescriptorCache;

    /**
     * Instantiates a new {@link IndexManager}.
     *
     * @param collectionName the collection name
     * @param nitriteConfig  the nitrite config
     */
    public IndexManager(String collectionName, NitriteConfig nitriteConfig) {
        this.collectionName = collectionName;
        this.nitriteConfig = nitriteConfig;
        this.nitriteStore = nitriteConfig.getNitriteStore();
        this.indexMetaMap = getIndexMetaMap();
        initialize();
    }

    /**
     * Checks if an index descriptor already exists on the fields.
     *
     * @param fields the fields
     * @return the boolean
     */
    public boolean hasIndexDescriptor(Fields fields) {
        if (!indexMetaMap.containsKey(fields)) return false;

        IndexMeta indexMeta = indexMetaMap.get(fields);
        return indexMeta != null;
    }

    /**
     * Gets all defined index descriptors for the collection.
     *
     * @return the index descriptors
     */
    public Collection<IndexDescriptor> getIndexDescriptors() {
        if (indexDescriptorCache == null || indexDescriptorCache.isEmpty()) {
            indexDescriptorCache = listIndexDescriptors();
        }
        return indexDescriptorCache;
    }

    public Collection<IndexDescriptor> findMatchingIndexDescriptors(Fields fields) {
        Collection<IndexDescriptor> indexDescriptors = getIndexDescriptors();

        for (IndexDescriptor indexDescriptor : indexDescriptors) {
            if (indexDescriptor.getIndexFields().startsWith(fields)) {
                indexDescriptors.add(indexDescriptor);
            }
        }

        return indexDescriptors;
    }

    public IndexDescriptor findExactIndexDescriptor(Fields fields) {
        IndexMeta meta = indexMetaMap.get(fields);
        if (meta != null) {
            return meta.getIndexDescriptor();
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        if (indexMetaMap != null) {
            indexMetaMap.close();
        }
    }

    /**
     * Is dirty index boolean.
     *
     * @param fields the fields
     * @return the boolean
     */
    boolean isDirtyIndex(Fields fields) {
        IndexMeta meta = indexMetaMap.get(fields);
        return meta != null && meta.getIsDirty().get();
    }

    /**
     * List index descriptors collection.
     *
     * @return the collection
     */
    Collection<IndexDescriptor> listIndexDescriptors() {
        Set<IndexDescriptor> indexSet = new LinkedHashSet<>();
        for (IndexMeta indexMeta : indexMetaMap.values()) {
            indexSet.add(indexMeta.getIndexDescriptor());
        }
        return Collections.unmodifiableSet(indexSet);
    }

    /**
     * Create index descriptor index descriptor.
     *
     * @param fields    the fields
     * @param indexType the index type
     * @return the index descriptor
     */
    IndexDescriptor createIndexDescriptor(Fields fields, String indexType) {
        validateIndexRequest(fields, indexType);
        IndexDescriptor index = new IndexDescriptor(indexType, fields, collectionName);

        IndexMeta indexMeta = new IndexMeta();
        indexMeta.setIndexDescriptor(index);
        indexMeta.setIsDirty(new AtomicBoolean(false));
        indexMeta.setIndexMap(deriveIndexMapName(index));

        indexMetaMap.put(fields, indexMeta);

        updateIndexDescriptorCache();
        return index;
    }

    /**
     * Drop index descriptor.
     *
     * @param fields the fields
     */
    void dropIndexDescriptor(Fields fields) {
        IndexMeta meta = indexMetaMap.get(fields);
        if (meta != null && meta.getIndexDescriptor() != null) {
            String indexMapName = meta.getIndexMap();
            NitriteMap<?, ?> indexMap = nitriteStore.openMap(indexMapName, Object.class, Object.class);
            indexMap.drop();
        }

        indexMetaMap.remove(fields);
        updateIndexDescriptorCache();
    }

    /**
     * Begin indexing.
     *
     * @param fields the fields
     */
    void beginIndexing(Fields fields) {
        markDirty(fields, true);
    }

    /**
     * End indexing.
     *
     * @param fields the fields
     */
    void endIndexing(Fields fields) {
        markDirty(fields, false);
    }

    private void initialize() {
        updateIndexDescriptorCache();
    }

    private void markDirty(Fields fields, boolean dirty) {
        IndexMeta meta = indexMetaMap.get(fields);
        if (meta != null && meta.getIndexDescriptor() != null) {
            meta.getIsDirty().set(dirty);
        }
    }

    private NitriteMap<Fields, IndexMeta> getIndexMetaMap() {
        String mapName = deriveIndexMetaMapName(this.collectionName);
        return this.nitriteStore.openMap(mapName, Fields.class, IndexMeta.class);
    }

    private void updateIndexDescriptorCache() {
        indexDescriptorCache = listIndexDescriptors();
    }

    private void validateIndexRequest(Fields fields, String indexType) {
        NitriteIndexer indexer = nitriteConfig.findIndexer(indexType);
        indexer.validateIndex(fields);
    }
}
