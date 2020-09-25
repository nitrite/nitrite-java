package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SecurityExceptionTest {
    @Test
    public void testConstructor() {
        SecurityException actualSecurityException = new SecurityException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.SecurityException: An error occurred",
                actualSecurityException.toString());
        assertEquals("An error occurred", actualSecurityException.getLocalizedMessage());
        assertNull(actualSecurityException.getCause());
        assertEquals("An error occurred", actualSecurityException.getMessage());
        assertEquals(0, actualSecurityException.getSuppressed().length);
    }
}

