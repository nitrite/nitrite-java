package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.collection.operation.IndexManager;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collection;

/**
 * A command to rename a {@link org.dizitart.no2.collection.NitriteCollection}.
 *
 * @author Anindya Chatterjee
 * @since 4.0
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

        IndexManager indexManager = new IndexManager(oldName, nitrite.getConfig());
        Collection<IndexDescriptor> indexEntries = indexManager.getIndexDescriptors();
        for (IndexDescriptor indexDescriptor : indexEntries) {
            Fields field = indexDescriptor.getIndexFields();
            String indexType = indexDescriptor.getIndexType();
            newOperations.createIndex(field, indexType);
        }

        operations.dropCollection();
    }
}
