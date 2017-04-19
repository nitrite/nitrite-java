package org.dizitart.no2.exceptions;

/**
 * Exception thrown during find operations due to
 * invalid filter configuration.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class FilterException extends NitriteException {
    /**
     * Instantiates a new Filter exception.
     *
     * @param message the message
     */
    public FilterException(ErrorMessage message) {
        super(message);
    }

    /**
     * Instantiates a new Filter exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public FilterException(ErrorMessage message, Throwable cause) {
        super(message, cause);
    }
}
