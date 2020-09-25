package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class PluginExceptionTest {
    @Test
    public void testConstructor() {
        PluginException actualPluginException = new PluginException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.PluginException: An error occurred", actualPluginException.toString());
        assertEquals("An error occurred", actualPluginException.getLocalizedMessage());
        assertNull(actualPluginException.getCause());
        assertEquals("An error occurred", actualPluginException.getMessage());
        assertEquals(0, actualPluginException.getSuppressed().length);
    }

    @Test
    public void testConstructor2() {
        Throwable throwable = new Throwable();
        assertSame((new PluginException("An error occurred", throwable)).getCause(), throwable);
    }
}

