package org.dizitart.no2.internals;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.store.NitriteMap;

/**
 * A factory class to create a {@link NitriteCollection}.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public class CollectionFactory {
    /**
     * Opens or creates a {@link NitriteCollection}.
     *
     * @param mapStore the map store
     * @param context  the {@link NitriteContext}
     * @return the {@link NitriteCollection}.
     */
    public static NitriteCollection open(NitriteMap<NitriteId, Document> mapStore,
                                         NitriteContext context) {
        return new DefaultNitriteCollection(mapStore, context);
    }
}
