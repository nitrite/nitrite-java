package org.dizitart.no2.exceptions;

/**
 * Exception thrown while mapping of {@link org.dizitart.no2.Document} from
 * objects fails or vice versa.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public class ObjectMappingException extends NitriteException {
    /**
     * Instantiates a new Object mapping exception.
     *
     * @param message the message
     */
    public ObjectMappingException(ErrorMessage message) {
        super(message);
    }
}
