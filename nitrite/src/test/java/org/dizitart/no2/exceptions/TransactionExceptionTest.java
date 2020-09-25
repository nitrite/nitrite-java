package org.dizitart.no2.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class TransactionExceptionTest {
    @Test
    public void testConstructor() {
        TransactionException actualTransactionException = new TransactionException("An error occurred");
        assertEquals("org.dizitart.no2.exceptions.TransactionException: An error occurred",
            actualTransactionException.toString());
        assertEquals("An error occurred", actualTransactionException.getLocalizedMessage());
        assertNull(actualTransactionException.getCause());
        assertEquals("An error occurred", actualTransactionException.getMessage());
        assertEquals(0, actualTransactionException.getSuppressed().length);
    }

    @Test
    public void testConstructor2() {
        Throwable throwable = new Throwable();
        assertSame((new TransactionException("An error occurred", throwable)).getCause(), throwable);
    }
}

