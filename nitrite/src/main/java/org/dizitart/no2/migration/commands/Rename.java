package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
@AllArgsConstructor
public class Rename extends BaseCommand implements Command {
    private final String oldName;
    private final String newName;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, oldName);

        if (nitriteStore.hasMap(newName)) {
            throw new InvalidOperationException("a collection with name " + newName + " already exists");
        }

        NitriteMap<NitriteId, Document> newMap = nitriteStore.openMap(newName, NitriteId.class, Document.class);
        CollectionOperations newOperations = new CollectionOperations(newName, newMap, nitrite.getConfig(), null);

        for (Pair<NitriteId, Document> entry : nitriteMap.entries()) {
            newMap.put(entry.getFirst(), entry.getSecond());
        }

        IndexCatalog indexCatalog = nitrite.getStore().getIndexCatalog();
        Collection<IndexEntry> indexEntries = indexCatalog.listIndexEntries(oldName);
        for (IndexEntry indexEntry : indexEntries) {
            String field = indexEntry.getField();
            String indexType = indexEntry.getIndexType();
            newOperations.createIndex(field, indexType, false);
        }

        operations.dropCollection();
    }
}
