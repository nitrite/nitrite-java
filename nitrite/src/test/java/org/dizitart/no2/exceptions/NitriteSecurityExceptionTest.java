package org.dizitart.no2.exceptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NitriteSecurityExceptionTest {
    @Test
    public void testConstructor() {
        NitriteSecurityException actualSecurityException = new NitriteSecurityException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.NitriteSecurityException: An error occurred",
                actualSecurityException.toString());
        assertEquals("An error occurred", actualSecurityException.getLocalizedMessage());
        assertNull(actualSecurityException.getCause());
        assertEquals("An error occurred", actualSecurityException.getMessage());
        assertEquals(0, actualSecurityException.getSuppressed().length);
    }
}

