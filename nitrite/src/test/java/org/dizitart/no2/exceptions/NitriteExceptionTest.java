package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class NitriteExceptionTest {
    @Test
    public void testConstructor() {
        NitriteException actualNitriteException = new NitriteException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.NitriteException: An error occurred", actualNitriteException.toString());
        assertEquals("An error occurred", actualNitriteException.getLocalizedMessage());
        assertNull(actualNitriteException.getCause());
        assertEquals("An error occurred", actualNitriteException.getMessage());
        assertEquals(0, actualNitriteException.getSuppressed().length);
    }

    @Test
    public void testConstructor2() {
        Throwable throwable = new Throwable();
        assertSame((new NitriteException("An error occurred", throwable)).getCause(), throwable);
    }
}

