package org.dizitart.no2.migration.commands;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

/**
 * @author Anindya Chatterjee
 */
abstract class BaseCommand implements Command {
    protected NitriteStore<?> nitriteStore;
    protected IndexCatalog indexCatalog;
    protected NitriteMap<NitriteId, Document> nitriteMap;
    protected CollectionOperations operations;

    void initialize(Nitrite nitrite, String collectionName) {
        nitriteStore = nitrite.getStore();
        indexCatalog = nitriteStore.getIndexCatalog();

        nitriteMap = nitriteStore.openMap(collectionName, NitriteId.class, Document.class);
        operations = new CollectionOperations(collectionName, nitriteMap, nitrite.getConfig(), null);
    }
}
