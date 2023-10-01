package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.operation.IndexManager;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexDescriptor;

import java.util.Collection;

/**
 * A command to rename a document field.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@AllArgsConstructor
public class RenameField extends BaseCommand {
    private final String collectionName;
    private final String oldName;
    private final String newName;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        try(IndexManager indexManager = new IndexManager(oldName, nitrite.getConfig())) {
            Fields oldField = Fields.withNames(oldName);
            Collection<IndexDescriptor> matchingIndexDescriptors
                = indexManager.findMatchingIndexDescriptors(oldField);

            for (Pair<NitriteId, Document> entry : nitriteMap.entries()) {
                Document document = entry.getSecond();
                if (document.containsKey(oldName)) {
                    Object value = document.get(oldName);
                    document.put(newName, value);
                    document.remove(oldName);

                    nitriteMap.put(entry.getFirst(), document);
                }
            }

            if (!matchingIndexDescriptors.isEmpty()) {
                for (IndexDescriptor matchingIndexDescriptor : matchingIndexDescriptors) {
                    String indexType = matchingIndexDescriptor.getIndexType();

                    Fields oldIndexFields = matchingIndexDescriptor.getFields();
                    Fields newIndexFields = getNewIndexFields(oldIndexFields, oldName, newName);
                    operations.dropIndex(matchingIndexDescriptor.getFields());
                    operations.createIndex(newIndexFields, indexType);
                }
            }
        }
    }

    private Fields getNewIndexFields(Fields oldIndexFields, String oldName, String newName) {
        Fields newIndexFields = new Fields();
        for (String fieldName : oldIndexFields.getFieldNames()) {
            if (fieldName.equals(oldName)) {
                newIndexFields.addField(newName);
            } else {
                newIndexFields.addField(fieldName);
            }
        }
        return newIndexFields;
    }
}
