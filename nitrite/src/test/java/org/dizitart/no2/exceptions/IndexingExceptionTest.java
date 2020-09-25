package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class IndexingExceptionTest {
    @Test
    public void testConstructor() {
        IndexingException actualIndexingException = new IndexingException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.IndexingException: An error occurred",
            actualIndexingException.toString());
        assertEquals("An error occurred", actualIndexingException.getLocalizedMessage());
        assertNull(actualIndexingException.getCause());
        assertEquals("An error occurred", actualIndexingException.getMessage());
        assertEquals(0, actualIndexingException.getSuppressed().length);
    }

    @Test
    public void testConstructor2() {
        Throwable throwable = new Throwable();
        assertSame((new IndexingException("An error occurred", throwable)).getCause(), throwable);
    }
}

