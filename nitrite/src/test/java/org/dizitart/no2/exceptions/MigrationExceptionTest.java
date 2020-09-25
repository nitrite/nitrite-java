package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MigrationExceptionTest {
    @Test
    public void testConstructor() {
        MigrationException actualMigrationException = new MigrationException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.MigrationException: An error occurred",
                actualMigrationException.toString());
        assertEquals("An error occurred", actualMigrationException.getLocalizedMessage());
        assertNull(actualMigrationException.getCause());
        assertEquals("An error occurred", actualMigrationException.getMessage());
        assertEquals(0, actualMigrationException.getSuppressed().length);
    }
}

