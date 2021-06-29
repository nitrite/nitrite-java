package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.Fields;

/**
 * A command to create an index.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@AllArgsConstructor
public class CreateIndex extends BaseCommand implements Command {
    private final String collectionName;
    private final Fields fields;
    private final String indexType;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        operations.createIndex(fields, indexType);
    }
}
