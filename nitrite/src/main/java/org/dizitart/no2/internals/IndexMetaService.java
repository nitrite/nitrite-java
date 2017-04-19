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
    private final NitriteMap<String, IndexMeta> indexMetadata;
    private final Map<String, Object> fieldLock;
    private final NitriteStore mvStore;

    IndexMetaService(NitriteMap<NitriteId, Document> underlyingMap) {
        this.underlyingMap = underlyingMap;
        this.mvStore = underlyingMap.getStore();
        String indexMetaName = getName();
        indexMetadata = mvStore.openMap(indexMetaName);
        this.fieldLock = new ConcurrentHashMap<>();
    }

    NitriteMap<NitriteId, Document> getUnderlyingMap() {
        return underlyingMap;
    }

    boolean hasIndex(String field) {
        return indexMetadata.containsKey(field);
    }

    boolean hasTextIndex(String field) {
        return indexMetadata.containsKey(field)
                && indexMetadata.get(field).index.getIndexType() == IndexType.Fulltext;
    }

    Index findIndex(String field) {
        IndexMeta meta = indexMetadata.get(field);
        if (meta != null) {
            return meta.index;
        }
        return null;
    }

    NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> getIndexMap(String field) {
        IndexMeta meta = indexMetadata.get(field);
        if (meta != null && meta.index != null) {
            return mvStore.openMap(meta.indexMap);
        }
        return null;
    }

    void markDirty(String field) {
        IndexMeta meta = indexMetadata.get(field);
        if (meta != null && meta.index != null) {
            meta.isDirty.set(true);
        }
    }

    void unmarkDirty(String field) {
        IndexMeta meta = indexMetadata.get(field);
        if (meta != null && meta.index != null) {
            meta.isDirty.set(false);
        }
    }

    synchronized boolean isDirtyIndex(String field) {
        IndexMeta meta = indexMetadata.get(field);
        return meta != null && meta.isDirty.get();
    }

    Collection<Index> listIndexes() {
        Set<Index> indexSet = new LinkedHashSet<>();
        for (IndexMeta indexMeta : indexMetadata.values()) {
            indexSet.add(indexMeta.index);
        }
        return Collections.unmodifiableSet(indexSet);
    }

    void dropIndex(String field) {
        IndexMeta meta = indexMetadata.get(field);
        if (meta != null && meta.index != null) {
            String indexMapName = meta.indexMap;
            mvStore.removeMap(mvStore.openMap(indexMapName));
        } else {
            throw new IndexingException(errorMessage(
                    field + " is not indexed", IE_DROP_NON_EXISTING_INDEX));
        }
        indexMetadata.remove(field);
    }

    void dropAll() {
        for (String field : indexMetadata.keySet()) {
            dropIndex(field);
        }
        mvStore.removeMap(indexMetadata);
    }

    Index createIndexMetadata(String field, IndexType indexType) {
        Index index = new Index(indexType, field, underlyingMap.getName());

        IndexMeta indexMeta = new IndexMeta();
        indexMeta.index = index;
        indexMeta.isDirty = new AtomicBoolean(false);
        indexMeta.indexMap = internalName(index);

        indexMetadata.put(field, indexMeta);

        return index;
    }

    synchronized Object getFieldLock(String field) {
        Object lock = fieldLock.get(field);
        if (lock != null) return lock;

        lock = new Object();
        fieldLock.put(field, lock);
        return lock;
    }

    private String getName() {
        return INDEX_META_PREFIX + INTERNAL_NAME_SEPARATOR + underlyingMap.getName();
    }

    @EqualsAndHashCode
    @ToString
    private static class IndexMeta implements Serializable {
        private Index index;
        private String indexMap;
        private AtomicBoolean isDirty;
    }
}