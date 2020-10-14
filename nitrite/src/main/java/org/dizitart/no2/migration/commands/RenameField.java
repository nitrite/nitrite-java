package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexDescriptor;
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

        boolean indexExists = indexCatalog.hasIndexDescriptor(collectionName, oldName);
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
            IndexDescriptor indexDescriptor = indexCatalog.findIndexDescriptor(collectionName, oldName);
            String indexType = indexDescriptor.getIndexType();

            operations.dropIndex(oldName);
            operations.createIndex(newName, indexType, false);
        }
    }
}
