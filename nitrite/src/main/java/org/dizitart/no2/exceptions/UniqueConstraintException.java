package org.dizitart.no2.exceptions;

/**
 * Exception thrown when any modification in a collection
 * violates unique constraint.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class UniqueConstraintException extends NitriteException {
    /**
     * Instantiates a new Unique constraint exception.
     *
     * @param message the message
     */
    public UniqueConstraintException(ErrorMessage message) {
        super(message);
    }
}
