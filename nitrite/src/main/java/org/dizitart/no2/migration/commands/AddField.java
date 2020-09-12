package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.migration.Generator;

/**
 * @author Anindya Chatterjee
 */
@AllArgsConstructor
public class AddField extends BaseCommand implements Command {
    private final String collectionName;
    private final String fieldName;
    private final Object defaultValue;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        IndexEntry indexEntry = indexCatalog.findIndexEntry(collectionName, fieldName);

        for (Pair<NitriteId, Document> pair : nitriteMap.entries()) {
            Document document = pair.getSecond();
            if (defaultValue instanceof Generator) {
                Generator<?> generator = (Generator<?>) defaultValue;
                document.put(fieldName, generator.generate(document));
            } else {
                document.put(fieldName, defaultValue);
            }
            nitriteMap.put(pair.getFirst(), document);
        }

        if (indexEntry != null) {
            operations.createIndex(fieldName, indexEntry.getIndexType(), false);
        }
    }
}
