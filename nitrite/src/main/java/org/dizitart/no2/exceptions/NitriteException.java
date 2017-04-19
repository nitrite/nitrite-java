package org.dizitart.no2.exceptions;

import lombok.Getter;

/**
 * Represents a generic nitrite database runtime error.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Getter
public class NitriteException extends RuntimeException {

    /**
     * Gets the {@link ErrorMessage} corresponds to this exception.
     *
     * @return the {@link ErrorMessage}.
     * */
    private ErrorMessage errorMessage;

    /**
     * Instantiates a new Nitrite exception.
     *
     * @param errorMessage the error message
     */
    public NitriteException(ErrorMessage errorMessage) {
        super(errorMessage.getErrorCode() + ": " + errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }

    /**
     * Instantiates a new Nitrite exception.
     *
     * @param errorMessage the error message
     * @param cause   the cause
     */
    public NitriteException(ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage.getErrorCode() + ": " + errorMessage.getMessage(), cause);
        this.errorMessage = errorMessage;
    }
}
