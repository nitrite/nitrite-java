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

import org.dizitart.no2.common.Fields;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexMeta;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
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

    public IndexDescriptor findIndexDescriptor(String collectionName, Fields fields) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(fields);
        if (meta != null) {
            return meta.getIndexDescriptor();
        }
        return null;
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
