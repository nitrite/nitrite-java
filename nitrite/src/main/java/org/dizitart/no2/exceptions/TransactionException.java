package org.dizitart.no2.exceptions;

/**
 * @author Anindya Chatterjee
 */
public class TransactionException extends NitriteException {
    public TransactionException(String errorMessage) {
        super(errorMessage);
    }

    public TransactionException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }
}
