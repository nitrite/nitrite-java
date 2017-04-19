package org.dizitart.no2.exceptions;

/**
 * Exception thrown when a database security error occurs.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public class SecurityException extends NitriteException {
    /**
     * Instantiates a new {@link SecurityException}.
     *
     * @param errorMessage the error message
     */
    public SecurityException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
