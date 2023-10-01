package org.dizitart.no2.transaction;

/**
 * An enumeration representing the possible states of a transaction.
 * 
 * @author Anindya Chatterjee
 * @since 4.0
 */
public enum TransactionState {
    /**
     * Transaction is active.
     */
    Active,

    /**
     * Transaction partially committed.
     */
    PartiallyCommitted,

    /**
     * Transaction fully committed.
     */
    Committed,

    /**
     * Transaction closed.
     */
    Closed,

    /**
     * Transaction failed and rolled back.
     */
    Failed,

    /**
     * Transaction aborted.
     */
    Aborted,
}
