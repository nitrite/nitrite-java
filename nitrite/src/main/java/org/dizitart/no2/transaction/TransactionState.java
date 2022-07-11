package org.dizitart.no2.transaction;

/**
 * The transaction state.
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
     * Transaction failed.
     */
    Failed,

    /**
     * Transaction aborted.
     */
    Aborted,
}
