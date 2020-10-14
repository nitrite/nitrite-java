package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.migration.TypeConverter;

/**
 * @author Anindya Chatterjee
 */
@AllArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class ChangeDataType extends BaseCommand implements Command {
    private final String collectionName;
    private final String fieldName;
    private final TypeConverter typeConverter;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        for (Pair<NitriteId, Document> entry : nitriteMap.entries()) {
            Document document = entry.getSecond();
            Object value = document.get(fieldName);
            Object newValue = typeConverter.convert(value);
            document.put(fieldName, newValue);

            nitriteMap.put(entry.getFirst(), document);
        }

        IndexDescriptor indexDescriptor = indexCatalog.findIndexDescriptor(collectionName, fieldName);
        if (indexDescriptor != null) {
            operations.rebuildIndex(indexDescriptor, false);
        }
    }
}