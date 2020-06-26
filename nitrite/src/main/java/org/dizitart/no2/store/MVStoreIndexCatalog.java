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

import org.dizitart.no2.index.IndexEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.Constants.*;

/**
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
class MVStoreIndexCatalog implements IndexCatalog {
    private final NitriteStore nitriteStore;

    MVStoreIndexCatalog(NitriteStore nitriteStore) {
        this.nitriteStore = nitriteStore;
    }

    @Override
    public boolean hasIndexEntry(String collectionName, String field) {
        NitriteMap<String, IndexMeta> indexMetaMap = getIndexMetaMap(collectionName);
        if (!indexMetaMap.containsKey(field)) return false;

        IndexMeta indexMeta = indexMetaMap.get(field);
        return indexMeta != null;
    }

    @Override
    public IndexEntry createIndexEntry(String collectionName, String field, String indexType) {
        IndexEntry index = new IndexEntry(indexType, field, collectionName);

        IndexMeta indexMeta = new IndexMeta();
        indexMeta.setIndexEntry(index);
        indexMeta.setIsDirty(new AtomicBoolean(false));
        indexMeta.setIndexMap(getIndexMapName(index));

        getIndexMetaMap(collectionName).put(field, indexMeta);

        return index;
    }

    @Override
    public IndexEntry findIndexEntry(String collectionName, String field) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(field);
        if (meta != null) {
            return meta.getIndexEntry();
        }
        return null;
    }

    @Override
    public boolean isDirtyIndex(String collectionName, String field) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(field);
        return meta != null && meta.getIsDirty().get();
    }

    @Override
    public Collection<IndexEntry> listIndexEntries(String collectionName) {
        Set<IndexEntry> indexSet = new LinkedHashSet<>();
        for (IndexMeta indexMeta : getIndexMetaMap(collectionName).values()) {
            indexSet.add(indexMeta.getIndexEntry());
        }
        return Collections.unmodifiableSet(indexSet);
    }

    @Override
    public void dropIndexEntry(String collectionName, String field) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(field);
        if (meta != null && meta.getIndexEntry() != null) {
            String indexMapName = meta.getIndexMap();
            nitriteStore.openMap(indexMapName).drop();
        }
        getIndexMetaMap(collectionName).remove(field);
    }

    @Override
    public void beginIndexing(String collectionName, String field) {
        markDirty(collectionName, field, true);
    }

    @Override
    public void endIndexing(String collectionName, String field) {
        markDirty(collectionName, field, false);
    }

    private NitriteMap<String, IndexMeta> getIndexMetaMap(String collectionName) {
        String indexMetaName = getIndexMetaName(collectionName);
        return nitriteStore.openMap(indexMetaName);
    }

    private String getIndexMetaName(String collectionName) {
        return INDEX_META_PREFIX + INTERNAL_NAME_SEPARATOR + collectionName;
    }

    private String getIndexMapName(IndexEntry index) {
        return INDEX_PREFIX +
            INTERNAL_NAME_SEPARATOR +
            index.getCollectionName() +
            INTERNAL_NAME_SEPARATOR +
            index.getField() +
            INTERNAL_NAME_SEPARATOR +
            index.getIndexType();
    }

    private void markDirty(String collectionName, String field, boolean dirty) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(field);
        if (meta != null && meta.getIndexEntry() != null) {
            meta.getIsDirty().set(dirty);
        }
    }
}
