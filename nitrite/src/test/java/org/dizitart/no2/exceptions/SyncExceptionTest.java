package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class SyncExceptionTest {
    @Test
    public void testConstructor() {
        Throwable throwable = new Throwable();
        assertSame((new SyncException("An error occurred", throwable)).getCause(), throwable);
    }

    @Test
    public void testConstructor2() {
        SyncException actualSyncException = new SyncException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.SyncException: An error occurred", actualSyncException.toString());
        assertEquals("An error occurred", actualSyncException.getLocalizedMessage());
        assertNull(actualSyncException.getCause());
        assertEquals("An error occurred", actualSyncException.getMessage());
        assertEquals(0, actualSyncException.getSuppressed().length);
    }
}

