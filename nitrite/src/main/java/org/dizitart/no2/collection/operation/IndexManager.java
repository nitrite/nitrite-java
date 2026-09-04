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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.util.IndexUtils.deriveCompositeIndexMapName;
import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;
import static org.dizitart.no2.common.util.IndexUtils.deriveUniqueIndexMapName;
import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMetaMapName;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
public class IndexManager implements AutoCloseable {
    private final NitriteConfig nitriteConfig;
    private final NitriteStore<?> nitriteStore;
    private final String collectionName;
    private final NitriteMap<Fields, IndexMeta> indexMetaMap;
    private Collection<IndexDescriptor> indexDescriptorCache;

    public IndexManager(String collectionName, NitriteConfig nitriteConfig) {
        this.collectionName = collectionName;
        this.nitriteConfig = nitriteConfig;
        this.nitriteStore = nitriteConfig.getNitriteStore();
        this.indexMetaMap = getIndexMetaMap();
        initialize();
    }

    public boolean hasIndexDescriptor(Fields fields) {
        return !findMatchingIndexDescriptors(fields).isEmpty();
    }

    public Collection<IndexDescriptor> getIndexDescriptors() {
        if (indexDescriptorCache == null) {
            indexDescriptorCache = listIndexDescriptors();
        }
        return indexDescriptorCache;
    }

    public Collection<IndexDescriptor> findMatchingIndexDescriptors(Fields fields) {
        List<IndexDescriptor> indexDescriptors = new ArrayList<>();

        for (IndexDescriptor indexDescriptor : getIndexDescriptors()) {
            if (indexDescriptor.getFields().startsWith(fields)) {
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

    public void markIndexDirty(IndexDescriptor indexDescriptor) {
        Fields fields = indexDescriptor.getFields();
        markDirty(fields, true);
    }

    @Override
    public void close() {
        // close all index maps
        if (!indexMetaMap.isClosed() && !indexMetaMap.isDropped()) {
            Iterable<IndexMeta> indexMetas = indexMetaMap.values();
            for (IndexMeta indexMeta : indexMetas) {
                if (indexMeta != null && indexMeta.getIndexDescriptor() != null) {
                    for (NitriteMap<?, ?> indexMap : existingLayoutMaps(indexMeta)) {
                        indexMap.close();
                    }
                }
            }
            // close index meta
            indexMetaMap.close();
        }
    }

    public void clearAll() {
        // clear and close all index maps
        if (!indexMetaMap.isClosed() && !indexMetaMap.isDropped()) {
            Iterable<IndexMeta> indexMetas = indexMetaMap.values();
            for (IndexMeta indexMeta : indexMetas) {
                if (indexMeta != null && indexMeta.getIndexDescriptor() != null) {
                    for (NitriteMap<?, ?> indexMap : existingLayoutMaps(indexMeta)) {
                        indexMap.clear();
                        indexMap.close();
                    }
                }
            }
        }
    }

    /**
     * The maps an index actually occupies in the store. {@link IndexMeta#getIndexMap()} records
     * the classic map name, but a single-field index may instead live in the composite layout
     * (non-unique) or the single-id layout (unique), each under a derived name of its own, and
     * an index in mid-migration can briefly have two. Closing, clearing or dropping only the
     * recorded map leaves the real one behind: after {@code clear()} its stale entries resolve
     * to deleted documents, and a unique index rejects the very keys the collection no longer
     * holds.
     */
    private List<NitriteMap<?, ?>> existingLayoutMaps(IndexMeta indexMeta) {
        List<String> names = new ArrayList<>();
        names.add(indexMeta.getIndexMap());
        IndexDescriptor descriptor = indexMeta.getIndexDescriptor();
        if (!descriptor.isCompoundIndex()) {
            names.add(deriveCompositeIndexMapName(descriptor));
            names.add(deriveUniqueIndexMapName(descriptor));
        }
        List<NitriteMap<?, ?>> maps = new ArrayList<>();
        for (String name : names) {
            if (nitriteStore.hasMap(name)) {
                maps.add(nitriteStore.openMap(name, Object.class, Object.class));
            }
        }
        return maps;
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
        Iterable<IndexMeta> iterable = indexMetaMap.values();
        for (IndexMeta indexMeta : iterable) {
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
            for (NitriteMap<?, ?> indexMap : existingLayoutMaps(meta)) {
                indexMap.drop();
            }
        }
        indexMetaMap.remove(fields);
        updateIndexDescriptorCache();
    }

    void dropIndexMeta() {
        indexMetaMap.drop();
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
            // put the meta back, otherwise the flag is only changed on the instance get()
            // returned and the store never learns the entry has to be written. The marker is
            // crash-recovery state, so both directions of it have to reach the disk.
            indexMetaMap.put(fields, meta);
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
