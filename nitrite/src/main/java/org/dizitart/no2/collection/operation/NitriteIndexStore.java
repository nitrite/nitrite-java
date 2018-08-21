/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.operation;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.index.Index;
import org.dizitart.no2.store.IndexStore;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.Constants.INDEX_META_PREFIX;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;
import static org.dizitart.no2.util.IndexUtils.internalName;

/**
 * @author Anindya Chatterjee.
 */
class NitriteIndexStore implements IndexStore {
    private final NitriteMap<NitriteId, Document> underlyingMap;
    private final NitriteStore mvStore;

    NitriteIndexStore(NitriteMap<NitriteId, Document> underlyingMap) {
        this.underlyingMap = underlyingMap;
        this.mvStore = underlyingMap.getStore();
    }

    @Override
    public boolean hasIndex(String field) {
        NitriteMap<String, IndexMeta> indexMetaMap = getIndexMetaMap();
        if (!indexMetaMap.containsKey(field)) return false;

        IndexMeta indexMeta = indexMetaMap.get(field);
        return indexMeta != null;
    }

    @Override
    public Index findIndex(String field) {
        IndexMeta meta = getIndexMetaMap().get(field);
        if (meta != null) {
            return meta.index;
        }
        return null;
    }

    @Override
    public NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> getIndexMap(String field) {
        IndexMeta meta = getIndexMetaMap().get(field);
        if (meta != null && meta.index != null) {
            return mvStore.openMap(meta.indexMap);
        }
        return null;
    }

    @Override
    public synchronized void mark(String field, boolean dirty) {
        IndexMeta meta = getIndexMetaMap().get(field);
        if (meta != null && meta.index != null) {
            meta.isDirty.set(dirty);
        }
    }

    @Override
    public synchronized boolean isDirtyIndex(String field) {
        IndexMeta meta = getIndexMetaMap().get(field);
        return meta != null && meta.isDirty.get();
    }

    @Override
    public Collection<Index> listIndexes() {
        Set<Index> indexSet = new LinkedHashSet<>();
        for (IndexMeta indexMeta : getIndexMetaMap().values()) {
            indexSet.add(indexMeta.index);
        }
        return Collections.unmodifiableSet(indexSet);
    }

    @Override
    public void dropIndex(String field) {
        IndexMeta meta = getIndexMetaMap().get(field);
        if (meta != null && meta.index != null) {
            String indexMapName = meta.indexMap;
            mvStore.openMap(indexMapName).drop();
        }
        getIndexMetaMap().remove(field);
    }

    @Override
    public void dropAll() {
        for (String field : getIndexMetaMap().keySet()) {
            if (field != null) {
                dropIndex(field);
            }
        }
        mvStore.removeMap(getIndexMetaMap());
    }

    @Override
    public Index createIndex(String field, IndexType indexType) {
        Index index = new Index(indexType, field, underlyingMap.getName());

        IndexMeta indexMeta = new IndexMeta();
        indexMeta.index = index;
        indexMeta.isDirty = new AtomicBoolean(false);
        indexMeta.indexMap = internalName(index);

        getIndexMetaMap().put(field, indexMeta);

        return index;
    }

    private String getName() {
        return INDEX_META_PREFIX + INTERNAL_NAME_SEPARATOR + underlyingMap.getName();
    }

    private NitriteMap<String, IndexMeta> getIndexMetaMap() {
        String indexMetaName = getName();
        return mvStore.openMap(indexMetaName);
    }

    @EqualsAndHashCode
    @ToString
    private static class IndexMeta implements Serializable {
        private Index index;
        private String indexMap;
        private AtomicBoolean isDirty;
    }
}