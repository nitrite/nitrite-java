package org.dizitart.no2.exceptions;

/**
 * Exception thrown when a problem encountered during replication.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public class SyncException extends NitriteException {

    /**
     * Instantiates a new {@link SyncException}.
     *
     * @param errorMessage the error message
     */
    public SyncException(ErrorMessage errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new {@link SyncException}.
     *
     * @param errorMessage the error message
     * @param cause        the cause
     */
    public SyncException(ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
