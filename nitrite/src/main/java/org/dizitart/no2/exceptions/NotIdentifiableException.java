package org.dizitart.no2.exceptions;

/**
 * Exception thrown when an object is not uniquely identifiable.
 *
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class NotIdentifiableException extends NitriteException {
    /**
     * Instantiates a new Not identifiable exception.
     *
     * @param message the message
     */
    public NotIdentifiableException(ErrorMessage message) {
        super(message);
    }
}
