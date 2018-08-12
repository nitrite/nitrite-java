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

package org.dizitart.no2.internals;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.Index;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.Constants.INDEX_META_PREFIX;
import static org.dizitart.no2.Constants.INTERNAL_NAME_SEPARATOR;
import static org.dizitart.no2.exceptions.ErrorCodes.IE_DROP_NON_EXISTING_INDEX;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.IndexUtils.internalName;

/**
 * @author Anindya Chatterjee.
 */
class IndexMetaService {
    private final NitriteMap<NitriteId, Document> underlyingMap;
    private final NitriteStore mvStore;

    IndexMetaService(NitriteMap<NitriteId, Document> underlyingMap) {
        this.underlyingMap = underlyingMap;
        this.mvStore = underlyingMap.getStore();
    }

    NitriteMap<NitriteId, Document> getUnderlyingMap() {
        return underlyingMap;
    }

    boolean hasIndex(String field) {
        return getIndexMetadata().containsKey(field)
            && getIndexMetadata().get(field) != null;
    }

    boolean hasTextIndex(String field) {
        return getIndexMetadata().containsKey(field)
                && getIndexMetadata().get(field).index.getIndexType() == IndexType.Fulltext;
    }

    Index findIndex(String field) {
        IndexMeta meta = getIndexMetadata().get(field);
        if (meta != null) {
            return meta.index;
        }
        return null;
    }

    NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> getIndexMap(String field) {
        IndexMeta meta = getIndexMetadata().get(field);
        if (meta != null && meta.index != null) {
            return mvStore.openMap(meta.indexMap);
        }
        return null;
    }

    void markDirty(String field) {
        IndexMeta meta = getIndexMetadata().get(field);
        if (meta != null && meta.index != null) {
            meta.isDirty.set(true);
        }
    }

    void unmarkDirty(String field) {
        IndexMeta meta = getIndexMetadata().get(field);
        if (meta != null && meta.index != null) {
            meta.isDirty.set(false);
        }
    }

    synchronized boolean isDirtyIndex(String field) {
        IndexMeta meta = getIndexMetadata().get(field);
        return meta != null && meta.isDirty.get();
    }

    Collection<Index> listIndexes() {
        Set<Index> indexSet = new LinkedHashSet<>();
        for (IndexMeta indexMeta : getIndexMetadata().values()) {
            indexSet.add(indexMeta.index);
        }
        return Collections.unmodifiableSet(indexSet);
    }

    void dropIndex(String field) {
        IndexMeta meta = getIndexMetadata().get(field);
        if (meta != null && meta.index != null) {
            String indexMapName = meta.indexMap;
            mvStore.removeMap(mvStore.openMap(indexMapName));
        } else {
            throw new IndexingException(errorMessage(
                    field + " is not indexed", IE_DROP_NON_EXISTING_INDEX));
        }
        getIndexMetadata().remove(field);
    }

    void dropAll() {
        for (String field : getIndexMetadata().keySet()) {
            if (field != null) {
                dropIndex(field);
            }
        }
        mvStore.removeMap(getIndexMetadata());
    }

    Index createIndexMetadata(String field, IndexType indexType) {
        Index index = new Index(indexType, field, underlyingMap.getName());

        IndexMeta indexMeta = new IndexMeta();
        indexMeta.index = index;
        indexMeta.isDirty = new AtomicBoolean(false);
        indexMeta.indexMap = internalName(index);

        getIndexMetadata().put(field, indexMeta);

        return index;
    }

    private String getName() {
        return INDEX_META_PREFIX + INTERNAL_NAME_SEPARATOR + underlyingMap.getName();
    }

    private NitriteMap<String, IndexMeta> getIndexMetadata() {
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