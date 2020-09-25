package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class UniqueConstraintExceptionTest {
    @Test
    public void testConstructor() {
        UniqueConstraintException actualUniqueConstraintException = new UniqueConstraintException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.UniqueConstraintException: An error occurred",
                actualUniqueConstraintException.toString());
        assertEquals("An error occurred", actualUniqueConstraintException.getLocalizedMessage());
        assertNull(actualUniqueConstraintException.getCause());
        assertEquals("An error occurred", actualUniqueConstraintException.getMessage());
        assertEquals(0, actualUniqueConstraintException.getSuppressed().length);
    }
}

