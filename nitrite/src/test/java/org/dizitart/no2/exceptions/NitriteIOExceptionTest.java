package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class NitriteIOExceptionTest {
    @Test
    public void testConstructor() {
        NitriteIOException actualNitriteIOException = new NitriteIOException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.NitriteIOException: An error occurred",
            actualNitriteIOException.toString());
        assertEquals("An error occurred", actualNitriteIOException.getLocalizedMessage());
        assertNull(actualNitriteIOException.getCause());
        assertEquals("An error occurred", actualNitriteIOException.getMessage());
        assertEquals(0, actualNitriteIOException.getSuppressed().length);
    }

    @Test
    public void testConstructor2() {
        Throwable throwable = new Throwable();
        assertSame((new NitriteIOException("An error occurred", throwable)).getCause(), throwable);
    }
}

