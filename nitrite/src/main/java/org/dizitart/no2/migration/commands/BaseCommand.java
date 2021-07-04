package org.dizitart.no2.migration.commands;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

/**
 * Represents a base command for database migration. It initializes
 * different components necessary to execute the migration steps
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
abstract class BaseCommand implements Command {
    /**
     * The nitrite store.
     */
    protected NitriteStore<?> nitriteStore;

    /**
     * The nitrite map.
     */
    protected NitriteMap<NitriteId, Document> nitriteMap;

    /**
     * The collection operations.
     */
    protected CollectionOperations operations;

    @Override
    public void close() {
        if (operations != null) {
            operations.close();
        }
    }

    /**
     * Initializes the database for migration.
     *
     * @param nitrite        the nitrite
     * @param collectionName the collection name
     */
    void initialize(Nitrite nitrite, String collectionName) {
        nitriteStore = nitrite.getStore();

        nitriteMap = nitriteStore.openMap(collectionName, NitriteId.class, Document.class);
        operations = new CollectionOperations(collectionName, nitriteMap, nitrite.getConfig(), null);
    }
}
