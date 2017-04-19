package org.dizitart.no2.exceptions;

/**
 * Exception thrown when a requested operation is not
 * allowed to be executed.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class InvalidOperationException extends NitriteException {
    /**
     * Instantiates a new Invalid operation exception.
     *
     * @param message the message
     */
    public InvalidOperationException(ErrorMessage message) {
        super(message);
    }
}
