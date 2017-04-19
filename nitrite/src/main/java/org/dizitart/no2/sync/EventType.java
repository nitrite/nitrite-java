package org.dizitart.no2.sync;

/**
 * Represents different types of replication events.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public enum EventType {
    /**
     * Replication started.
     */
    STARTED,
    /**
     * Replication is in progress.
     */
    IN_PROGRESS,
    /**
     * Replication has been completed.
     */
    COMPLETED,
    /**
     * Replication has been canceled by user.
     */
    CANCELED,
    /**
     * Replication has been stopped by user.
     */
    STOPPED,
    /**
     * Replication has failed with an exception.
     */
    REPLICATION_ERROR,
}
