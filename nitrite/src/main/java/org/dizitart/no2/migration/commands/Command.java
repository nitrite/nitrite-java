package org.dizitart.no2.migration.commands;

import org.dizitart.no2.Nitrite;

/**
 * @author Anindya Chatterjee
 */
public interface Command {
    void execute(Nitrite nitrite);
}
