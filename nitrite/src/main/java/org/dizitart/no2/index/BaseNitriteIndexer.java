package org.dizitart.no2.index;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.UnknownType;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.dizitart.no2.common.Constants.INDEX_PREFIX;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;

/**
 * @author Anindya Chatterjee
 */
public abstract class BaseNitriteIndexer implements NitriteIndexer {

    @Override
    public void dropIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        NitriteMap<?, ?> indexMap;
        if (isCompoundIndex(indexDescriptor)) {
            indexMap = getCompoundIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), UnknownType.class);
        } else {
            indexMap = getSimpleIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), UnknownType.class);
        }
        indexMap.clear();
        indexMap.drop();
    }

    public boolean isCompoundIndex(IndexDescriptor indexDescriptor) {
        return indexDescriptor.getFields().getFieldNames().size() > 1;
    }

    @SuppressWarnings("rawtypes")
    public NitriteMap<Comparable, NavigableSet<?>> getSimpleIndexMap(IndexDescriptor indexDescriptor,
                                                                     NitriteStore<?> nitriteStore,
                                                                     Class<?> keyType) {
        String mapName = getIndexMapName(indexDescriptor);
        return nitriteStore.openMap(mapName, keyType, ConcurrentSkipListSet.class);
    }

    @SuppressWarnings("rawtypes")
    public NitriteMap<Comparable, NavigableMap<?, ?>> getCompoundIndexMap(IndexDescriptor indexDescriptor,
                                                                          NitriteStore<?> nitriteStore,
                                                                          Class<?> keyType) {
        String mapName = getIndexMapName(indexDescriptor);
        return nitriteStore.openMap(mapName, keyType, ConcurrentSkipListMap.class);
    }

    protected String getIndexMapName(IndexDescriptor indexDescriptor) {
        return INDEX_PREFIX +
            INTERNAL_NAME_SEPARATOR +
            indexDescriptor.getCollectionName() +
            INTERNAL_NAME_SEPARATOR +
            indexDescriptor.getFields().getEncodedName() +
            INTERNAL_NAME_SEPARATOR +
            getIndexType();
    }
}
