package org.dizitart.no2.filters;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.store.NitriteMap;

/**
 * @author Anindya Chatterjee
 */
public class IndexedQuerySupport {
    private ComparableIndexer comparableIndexer;

    public IndexedQuerySupport(ComparableIndexer comparableIndexer) {
        this.comparableIndexer = comparableIndexer;
    }

    public RecordStream<NitriteId> findByFilter(NitriteMap<?, ?> indexMap, NitriteFilter filter, NitriteConfig nitriteConfig) {

        return null;
    }

    public FieldValues calculateFieldValues(NitriteFilter filter) {

    }
}
