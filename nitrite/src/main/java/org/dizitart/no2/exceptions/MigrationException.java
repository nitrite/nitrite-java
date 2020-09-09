package org.dizitart.no2.exceptions;

/**
 * @author Anindya Chatterjee
 */
public class MigrationException extends NitriteException {
    public MigrationException(String errorMessage) {
        super(errorMessage);
    }
}
