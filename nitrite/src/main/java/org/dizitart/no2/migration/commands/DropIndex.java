package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 */
@AllArgsConstructor
public class DropIndex extends BaseCommand implements Command {
    private final String collectionName;
    private final String fieldName;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);

        if (isNullOrEmpty(fieldName)) {
            operations.dropAllIndices();
        } else {
            operations.dropIndex(fieldName);
        }
    }
}
