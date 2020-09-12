package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.index.IndexType;

/**
 * @author Anindya Chatterjee
 */
@AllArgsConstructor
public class ChangeIdField extends BaseCommand implements Command {
    private final String collectionName;
    private final String oldFieldName;
    private final String newFieldName;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        boolean hasIndex = operations.hasIndex(oldFieldName);
        if (hasIndex) {
            operations.dropIndex(oldFieldName);
        }

        operations.createIndex(newFieldName, IndexType.Unique, false);
    }
}