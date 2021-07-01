package org.dizitart.no2.migration.commands;

import org.dizitart.no2.Nitrite;

/**
 * Represents a database migration command.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface Command extends AutoCloseable {
    /**
     * Executes a migration step on the database.
     *
     * @param nitrite the nitrite database instance
     */
    void execute(Nitrite nitrite);

    default void close() {
        // this is just to make Command a functional interface
        // and make close() not throw checked exception
    }
}
