package org.dizitart.no2.exceptions;

import org.junit.Test;

import static org.dizitart.no2.exceptions.ErrorMessage.PREFIX;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class ErrorMessageTest {

    @Test
    public void testErrorMessage() {
        ErrorMessage message = errorMessage("test", 100);
        assertEquals(message.getMessage(), "test");
        assertEquals(message.getErrorCode(), PREFIX + 100);
    }

    @Test
    public void testIsEqual() {
        ErrorMessage message = errorMessage("test", 100);
        assertTrue(message.isEqual(100));
    }
}
