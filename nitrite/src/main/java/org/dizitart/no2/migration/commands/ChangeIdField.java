package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.index.IndexType;

/**
 * A command to change the id fields of an entity in
 * an {@link org.dizitart.no2.repository.ObjectRepository}.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@AllArgsConstructor
public class ChangeIdField extends BaseCommand implements Command {
    private final String collectionName;
    private final Fields oldFields;
    private final Fields newFields;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        boolean hasIndex = operations.hasIndex(oldFields);
        if (hasIndex) {
            operations.dropIndex(oldFields);
        }

        operations.createIndex(newFields, IndexType.Unique);
    }
}