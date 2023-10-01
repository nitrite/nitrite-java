package org.dizitart.no2.migration.commands;

import org.dizitart.no2.Nitrite;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface Command extends AutoCloseable {
    void execute(Nitrite nitrite);

    default void close() {
        // this is just to make Command a functional interface
        // and make close() not throw checked exception
    }
}
