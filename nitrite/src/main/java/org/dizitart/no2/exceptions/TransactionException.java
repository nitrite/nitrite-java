package org.dizitart.no2.exceptions;

/**
 * Exception thrown when a transaction fails.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class TransactionException extends NitriteException {
    /**
     * Instantiates a new Transaction exception.
     *
     * @param errorMessage the error message
     */
    public TransactionException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new Transaction exception.
     *
     * @param errorMessage the error message
     * @param error        the error
     */
    public TransactionException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
