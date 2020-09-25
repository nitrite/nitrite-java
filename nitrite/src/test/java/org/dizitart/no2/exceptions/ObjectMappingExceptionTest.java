package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ObjectMappingExceptionTest {
    @Test
    public void testConstructor() {
        Throwable throwable = new Throwable();
        assertSame((new ObjectMappingException("An error occurred", throwable)).getCause(), throwable);
    }

    @Test
    public void testConstructor2() {
        ObjectMappingException actualObjectMappingException = new ObjectMappingException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.ObjectMappingException: An error occurred",
            actualObjectMappingException.toString());
        assertEquals("An error occurred", actualObjectMappingException.getLocalizedMessage());
        assertNull(actualObjectMappingException.getCause());
        assertEquals("An error occurred", actualObjectMappingException.getMessage());
        assertEquals(0, actualObjectMappingException.getSuppressed().length);
    }
}

