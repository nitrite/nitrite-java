package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class NotIdentifiableExceptionTest {
    @Test
    public void testConstructor() {
        NotIdentifiableException actualNotIdentifiableException = new NotIdentifiableException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.NotIdentifiableException: An error occurred",
                actualNotIdentifiableException.toString());
        assertEquals("An error occurred", actualNotIdentifiableException.getLocalizedMessage());
        assertNull(actualNotIdentifiableException.getCause());
        assertEquals("An error occurred", actualNotIdentifiableException.getMessage());
        assertEquals(0, actualNotIdentifiableException.getSuppressed().length);
    }
}

