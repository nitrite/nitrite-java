package org.dizitart.no2.migration.commands;

import lombok.AllArgsConstructor;
import org.dizitart.no2.Nitrite;

/**
 * @author Anindya Chatterjee
 */
@AllArgsConstructor
public class Drop extends BaseCommand implements Command {
    private final String collectionName;

    @Override
    public void execute(Nitrite nitrite) {
        initialize(nitrite, collectionName);
        operations.dropCollection();
    }
}
