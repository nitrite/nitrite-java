package org.dizitart.no2.transaction;

/**
 * @author Anindya Chatterjee
 */
public enum State {
    Active,
    PartiallyCommitted,
    Committed,
    Closed,
    Failed,
    Aborted,
}
