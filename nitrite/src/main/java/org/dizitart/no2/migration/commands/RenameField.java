package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.store.IndexCatalog;

/**
 * @author Anindya Chatterjee
 */
@AllArgsConstructor
public class RenameField extends BaseCommand implements Command {
    private final String collectionName;
    private final String oldName;
    private final String newName;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        boolean indexExists = indexCatalog.hasIndexEntry(collectionName, oldName);
        for (Pair<NitriteId, Document> entry : nitriteMap.entries()) {
            Document document = entry.getSecond();
            if (document.containsKey(oldName)) {
                Object value = document.get(oldName);
                document.put(newName, value);
                document.remove(oldName);

                nitriteMap.put(entry.getFirst(), document);
            }
        }

        if (indexExists) {
            IndexCatalog indexCatalog = nitrite.getStore().getIndexCatalog();
            IndexEntry indexEntry = indexCatalog.findIndexEntry(collectionName, oldName);
            String indexType = indexEntry.getIndexType();

            operations.dropIndex(oldName);
            operations.createIndex(newName, indexType, false);
        }
    }
}
