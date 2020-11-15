package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.store.IndexCatalog;

import static org.dizitart.no2.common.Fields.single;

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

        IndexCatalog indexCatalog = nitrite.getStore().getIndexCatalog();
        boolean indexExists = indexCatalog.hasIndexDescriptor(collectionName, single(oldName));
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
            IndexDescriptor indexDescriptor = indexCatalog.findIndexDescriptorExact(collectionName, single(oldName));
            String indexType = indexDescriptor.getIndexType();

            operations.dropIndex(single(oldName));
            operations.createIndex(single(newName), indexType, false);
        }
    }
}
