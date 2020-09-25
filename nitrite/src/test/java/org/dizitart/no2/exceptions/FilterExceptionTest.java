package org.dizitart.no2.exceptions;

import org.junit.Test;

import static org.junit.Assert.*;

public class FilterExceptionTest {
    @Test
    public void testConstructor() {
        FilterException actualFilterException = new FilterException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.FilterException: An error occurred", actualFilterException.toString());
        assertEquals("An error occurred", actualFilterException.getLocalizedMessage());
        assertNull(actualFilterException.getCause());
        assertEquals("An error occurred", actualFilterException.getMessage());
        assertEquals(0, actualFilterException.getSuppressed().length);
    }

    @Test
    public void testConstructor2() {
        Throwable throwable = new Throwable();
        assertSame((new FilterException("An error occurred", throwable)).getCause(), throwable);
    }
}

