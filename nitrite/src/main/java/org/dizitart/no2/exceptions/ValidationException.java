package org.dizitart.no2.exceptions;

/**
 * Exception thrown when a validation fails.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public class ValidationException extends NitriteException {

    /**
     * Instantiates a new {@link ValidationException}.
     *
     * @param errorMessage the error message
     */
    public ValidationException(ErrorMessage errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new {@link ValidationException}.
     *
     * @param errorMessage the error message
     * @param cause        the cause
     */
    public ValidationException(ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
