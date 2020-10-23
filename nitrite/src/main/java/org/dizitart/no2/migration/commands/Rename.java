package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexDescriptor;
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

        NitriteMap<NitriteId, Document> newMap = nitriteStore.openMap(newName, NitriteId.class, Document.class);
        CollectionOperations newOperations = new CollectionOperations(newName, newMap, nitrite.getConfig(), null);

        for (Pair<NitriteId, Document> entry : nitriteMap.entries()) {
            newMap.put(entry.getFirst(), entry.getSecond());
        }

        IndexCatalog indexCatalog = nitrite.getStore().getIndexCatalog();
        Collection<IndexDescriptor> indexEntries = indexCatalog.listIndexDescriptors(oldName);
        for (IndexDescriptor indexDescriptor : indexEntries) {
            String field = indexDescriptor.getIndexFields();
            String indexType = indexDescriptor.getIndexType();
            newOperations.createIndex(field, indexType, false);
        }

        operations.dropCollection();
    }
}
