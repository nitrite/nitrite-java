package org.dizitart.no2.exceptions;

/**
 * Exception thrown when a {@link org.dizitart.no2.Document}
 * does not have any {@link org.dizitart.no2.NitriteId} associated
 * with it or it has invalid/incompatible {@link org.dizitart.no2.NitriteId}.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class InvalidIdException extends NitriteException {
    /**
     * Instantiates a new Invalid id exception.
     *
     * @param message the message
     */
    public InvalidIdException(ErrorMessage message) {
        super(message);
    }

    /**
     * Instantiates a new Invalid id exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public InvalidIdException(ErrorMessage message, Throwable cause) {
        super(message, cause);
    }
}
