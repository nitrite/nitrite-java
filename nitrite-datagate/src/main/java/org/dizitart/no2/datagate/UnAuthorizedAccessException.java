package org.dizitart.no2.datagate;

/**
 * An error representing un-authorized access to a collection.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class UnAuthorizedAccessException extends RuntimeException {
    public UnAuthorizedAccessException(String message) {
        super(message);
    }
}
