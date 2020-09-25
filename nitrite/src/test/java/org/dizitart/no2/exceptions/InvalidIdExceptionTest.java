package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class InvalidIdExceptionTest {
    @Test
    public void testConstructor() {
        InvalidIdException actualInvalidIdException = new InvalidIdException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.InvalidIdException: An error occurred",
            actualInvalidIdException.toString());
        assertEquals("An error occurred", actualInvalidIdException.getLocalizedMessage());
        assertNull(actualInvalidIdException.getCause());
        assertEquals("An error occurred", actualInvalidIdException.getMessage());
        assertEquals(0, actualInvalidIdException.getSuppressed().length);
    }

    @Test
    public void testConstructor2() {
        Throwable throwable = new Throwable();
        assertSame((new InvalidIdException("An error occurred", throwable)).getCause(), throwable);
    }
}

