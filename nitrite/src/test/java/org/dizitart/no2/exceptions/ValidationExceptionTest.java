package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ValidationExceptionTest {
    @Test
    public void testConstructor() {
        Throwable throwable = new Throwable();
        assertSame((new ValidationException("An error occurred", throwable)).getCause(), throwable);
    }

    @Test
    public void testConstructor2() {
        ValidationException actualValidationException = new ValidationException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.ValidationException: An error occurred",
            actualValidationException.toString());
        assertEquals("An error occurred", actualValidationException.getLocalizedMessage());
        assertNull(actualValidationException.getCause());
        assertEquals("An error occurred", actualValidationException.getMessage());
        assertEquals(0, actualValidationException.getSuppressed().length);
    }
}

