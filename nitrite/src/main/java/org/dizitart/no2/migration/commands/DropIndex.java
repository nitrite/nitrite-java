package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.Fields;

/**
 * A command to drop an index.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@AllArgsConstructor
public class DropIndex extends BaseCommand implements Command {
    private final String collectionName;
    private final Fields fields;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        if (fields == null) {
            operations.dropAllIndices();
        } else {
            operations.dropIndex(fields);
        }
    }
}
