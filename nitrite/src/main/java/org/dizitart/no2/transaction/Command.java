package org.dizitart.no2.transaction;

/**
 * Represents an operation in a transaction.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
interface Command {
    /**
     * Executes the command during transaction commit or rollback.
     */
    void execute();
}
