package org.dizitart.no2.exceptions;

/**
 * Represents a generic database I/O error.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public class NitriteIOException extends NitriteException {
    /**
     * Instantiates a new {@link NitriteIOException}.
     *
     * @param errorMessage the error message
     */
    public NitriteIOException(ErrorMessage errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new {@link NitriteIOException}.
     *
     * @param message the message
     * @param cause   the cause
     */
    public NitriteIOException(ErrorMessage message, Throwable cause) {
        super(message, cause);
    }
}
