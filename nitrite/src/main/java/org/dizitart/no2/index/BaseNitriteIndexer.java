package org.dizitart.no2.index;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.UnknownType;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Anindya Chatterjee
 */
public abstract class BaseNitriteIndexer implements NitriteIndexer {

    @Override
    public void dropIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        NitriteMap<?, ?> indexMap;
        if (indexDescriptor.isCompoundIndex()) {
            indexMap = getCompoundIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), UnknownType.class);
        } else {
            indexMap = getSimpleIndexMap(indexDescriptor, nitriteConfig.getNitriteStore(), UnknownType.class);
        }
        indexMap.clear();
        indexMap.drop();
    }

    @SuppressWarnings("rawtypes")
    public NitriteMap<Comparable, NavigableSet<?>> getSimpleIndexMap(IndexDescriptor indexDescriptor,
                                                                     NitriteStore<?> nitriteStore,
                                                                     Class<?> keyType) {
        String mapName = getIndexMapName(indexDescriptor, nitriteStore);
        return nitriteStore.openMap(mapName, keyType, ConcurrentSkipListSet.class);
    }

    @SuppressWarnings("rawtypes")
    public NitriteMap<Comparable, NavigableMap<?, ?>> getCompoundIndexMap(IndexDescriptor indexDescriptor,
                                                                          NitriteStore<?> nitriteStore,
                                                                          Class<?> keyType) {
        String mapName = getIndexMapName(indexDescriptor, nitriteStore);
        return nitriteStore.openMap(mapName, keyType, ConcurrentSkipListMap.class);
    }

    protected String getIndexMapName(IndexDescriptor indexDescriptor, NitriteStore<?> nitriteStore) {
        return nitriteStore.getIndexCatalog().getIndexMapName(indexDescriptor);
    }
}
