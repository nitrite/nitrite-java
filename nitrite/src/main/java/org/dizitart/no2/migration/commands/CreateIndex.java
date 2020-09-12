package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;

/**
 * @author Anindya Chatterjee
 */
@AllArgsConstructor
public class CreateIndex extends BaseCommand implements Command {
    private final String collectionName;
    private final String fieldName;
    private final String indexType;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        operations.createIndex(fieldName, indexType, false);
    }
}
