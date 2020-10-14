package org.dizitart.no2.sync.message;

import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorMessageTest {
    @Test
    public void testCanEqual() {
        assertFalse((new ErrorMessage()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setHeader(new MessageHeader());
        assertFalse((new ErrorMessage()).equals(errorMessage));
    }

    @Test
    public void testEquals2() {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setHeader(new MessageHeader());
        ErrorMessage errorMessage1 = new ErrorMessage();
        errorMessage1.setHeader(new MessageHeader());
        assertTrue(errorMessage.equals(errorMessage1));
    }

    @Test
    public void testEquals3() {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setError("An error occurred");
        assertFalse(errorMessage.equals(new ErrorMessage()));
    }

    @Test
    public void testEquals4() {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setError("An error occurred");
        ErrorMessage errorMessage1 = new ErrorMessage();
        errorMessage1.setError("An error occurred");
        errorMessage1.setHeader(null);
        assertTrue(errorMessage.equals(errorMessage1));
    }

    @Test
    public void testEquals5() {
        ErrorMessage errorMessage = new ErrorMessage();
        assertTrue(errorMessage.equals(new ErrorMessage()));
    }

    @Test
    public void testEquals6() {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setError("An error occurred");
        assertFalse((new ErrorMessage()).equals(errorMessage));
    }

    @Test
    public void testEquals7() {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setHeader(new MessageHeader());
        assertFalse(errorMessage.equals(new ErrorMessage()));
    }

    @Test
    public void testEquals8() {
        assertFalse((new ErrorMessage()).equals("o"));
    }

    @Test
    public void testSetError() {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setError("An error occurred");
        assertEquals("An error occurred", errorMessage.getError());
    }

    @Test
    public void testSetHeader() {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setHeader(new MessageHeader());
        assertEquals("ErrorMessage(header=MessageHeader(id=null, correlationId=null, collection=null, userName=null,"
            + " timestamp=null, messageType=null, origin=null), error=null)", errorMessage.toString());
    }

    @Test
    public void testToString() {
        assertEquals("ErrorMessage(header=null, error=null)", (new ErrorMessage()).toString());
    }
}

