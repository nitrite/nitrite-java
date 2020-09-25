package org.dizitart.no2.sync.message;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class MessageHeaderTest {
    @Test
    public void testCanEqual() {
        assertFalse((new MessageHeader()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setId("42");
        assertFalse(messageHeader.equals(new MessageHeader()));
    }

    @Test
    public void testEquals10() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setUserName("janedoe");
        assertFalse(messageHeader.equals(new MessageHeader()));
    }

    @Test
    public void testEquals11() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCorrelationId("42");
        assertFalse((new MessageHeader()).equals(messageHeader));
    }

    @Test
    public void testEquals12() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCorrelationId("42");
        assertFalse(messageHeader.equals(new MessageHeader()));
    }

    @Test
    public void testEquals13() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCollection("collection");
        assertFalse(messageHeader.equals(new MessageHeader()));
    }

    @Test
    public void testEquals14() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setOrigin("origin");
        assertFalse((new MessageHeader()).equals(messageHeader));
    }

    @Test
    public void testEquals15() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCollection("collection");
        assertFalse((new MessageHeader()).equals(messageHeader));
    }

    @Test
    public void testEquals16() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setId("42");
        assertFalse((new MessageHeader()).equals(messageHeader));
    }

    @Test
    public void testEquals2() {
        MessageHeader messageHeader = new MessageHeader();
        assertTrue(messageHeader.equals(new MessageHeader()));
    }

    @Test
    public void testEquals3() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setUserName("janedoe");
        assertFalse((new MessageHeader()).equals(messageHeader));
    }

    @Test
    public void testEquals4() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setMessageType(MessageType.Error);
        assertFalse(messageHeader.equals(new MessageHeader()));
    }

    @Test
    public void testEquals5() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setMessageType(MessageType.Error);
        assertFalse((new MessageHeader()).equals(messageHeader));
    }

    @Test
    public void testEquals6() {
        assertFalse((new MessageHeader()).equals("o"));
    }

    @Test
    public void testEquals7() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setOrigin("origin");
        assertFalse(messageHeader.equals(new MessageHeader()));
    }

    @Test
    public void testEquals8() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setTimestamp(10L);
        assertFalse(messageHeader.equals(new MessageHeader()));
    }

    @Test
    public void testEquals9() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setTimestamp(10L);
        assertFalse((new MessageHeader()).equals(messageHeader));
    }

    @Test
    public void testHashCode() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setId("42");
        assertEquals(368163037, messageHeader.hashCode());
    }

    @Test
    public void testHashCode2() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCorrelationId("42");
        assertEquals(-2114525181, messageHeader.hashCode());
    }

    @Test
    public void testHashCode3() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setTimestamp(10L);
        assertEquals(64089785, messageHeader.hashCode());
    }

    @Test
    public void testHashCode4() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setUserName("janedoe");
        assertEquals(662096149, messageHeader.hashCode());
    }

    @Test
    public void testHashCode5() {
        assertEquals(64204658, (new MessageHeader()).hashCode());
    }

    @Test
    public void testHashCode6() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCollection("collection");
        assertEquals(-1690661067, messageHeader.hashCode());
    }

    @Test
    public void testHashCode7() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setOrigin("origin");
        assertEquals(-944415123, messageHeader.hashCode());
    }

    @Test
    public void testSetCollection() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCollection("collection");
        assertEquals("collection", messageHeader.getCollection());
    }

    @Test
    public void testSetCorrelationId() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCorrelationId("42");
        assertEquals("42", messageHeader.getCorrelationId());
    }

    @Test
    public void testSetId() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setId("42");
        assertEquals("42", messageHeader.getId());
    }

    @Test
    public void testSetMessageType() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setMessageType(MessageType.Error);
        assertEquals(MessageType.Error, messageHeader.getMessageType());
    }

    @Test
    public void testSetOrigin() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setOrigin("origin");
        assertEquals("origin", messageHeader.getOrigin());
    }

    @Test
    public void testSetTimestamp() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setTimestamp(10L);
        assertEquals(10L, messageHeader.getTimestamp().longValue());
    }

    @Test
    public void testSetUserName() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setUserName("janedoe");
        assertEquals("janedoe", messageHeader.getUserName());
    }

    @Test
    public void testToString() {
        assertEquals(
            "MessageHeader(id=null, correlationId=null, collection=null, userName=null, timestamp=null, messageType=null,"
                + " origin=null)",
            (new MessageHeader()).toString());
    }
}

