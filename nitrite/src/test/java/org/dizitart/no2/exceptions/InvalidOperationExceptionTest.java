package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class InvalidOperationExceptionTest {
    @Test
    public void testConstructor() {
        InvalidOperationException actualInvalidOperationException = new InvalidOperationException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.InvalidOperationException: An error occurred",
                actualInvalidOperationException.toString());
        assertEquals("An error occurred", actualInvalidOperationException.getLocalizedMessage());
        assertNull(actualInvalidOperationException.getCause());
        assertEquals("An error occurred", actualInvalidOperationException.getMessage());
        assertEquals(0, actualInvalidOperationException.getSuppressed().length);
    }
}

