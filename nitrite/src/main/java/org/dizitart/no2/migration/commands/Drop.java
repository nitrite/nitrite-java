package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;

/**
 * A command to drop a nitrite collection.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@AllArgsConstructor
public class Drop extends BaseCommand {
    private final String collectionName;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);
        operations.dropCollection();
    }
}
