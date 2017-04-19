package org.dizitart.no2.exceptions;

/**
 * Exception thrown while handling with nitrite database index.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class IndexingException extends NitriteException {
    /**
     * Instantiates a new Indexing exception.
     *
     * @param message the message
     */
    public IndexingException(ErrorMessage message) {
        super(message);
    }

    /**
     * Instantiates a new Indexing exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public IndexingException(ErrorMessage message, Throwable cause) {
        super(message, cause);
    }
}
