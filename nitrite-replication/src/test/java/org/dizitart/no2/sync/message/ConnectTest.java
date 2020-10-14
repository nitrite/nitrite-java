package org.dizitart.no2.sync.message;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConnectTest {
    @Test
    public void testCanEqual() {
        assertFalse((new Connect()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        Connect connect = new Connect();
        connect.setAuthToken("ABC123");
        assertFalse(connect.equals(new Connect()));
    }

    @Test
    public void testEquals2() {
        Connect connect = new Connect();
        assertTrue(connect.equals(new Connect()));
    }

    @Test
    public void testEquals3() {
        Connect connect = new Connect();
        connect.setAuthToken("ABC123");
        assertFalse((new Connect()).equals(connect));
    }

    @Test
    public void testEquals4() {
        Connect connect = new Connect();
        connect.setHeader(new MessageHeader());
        assertFalse(connect.equals(new Connect()));
    }

    @Test
    public void testEquals5() {
        Connect connect = new Connect();
        connect.setHeader(new MessageHeader());
        assertFalse((new Connect()).equals(connect));
    }

    @Test
    public void testEquals6() {
        Connect connect = new Connect();
        connect.setHeader(new MessageHeader());
        Connect connect1 = new Connect();
        connect1.setHeader(new MessageHeader());
        assertTrue(connect.equals(connect1));
    }

    @Test
    public void testEquals7() {
        Connect connect = new Connect();
        connect.setAuthToken("ABC123");
        Connect connect1 = new Connect();
        connect1.setAuthToken("ABC123");
        connect1.setHeader(null);
        assertTrue(connect.equals(connect1));
    }

    @Test
    public void testEquals8() {
        assertFalse((new Connect()).equals("o"));
    }

    @Test
    public void testSetAuthToken() {
        Connect connect = new Connect();
        connect.setAuthToken("ABC123");
        assertEquals("ABC123", connect.getAuthToken());
    }

    @Test
    public void testSetHeader() {
        Connect connect = new Connect();
        connect.setHeader(new MessageHeader());
        assertEquals(
                "Connect(header=MessageHeader(id=null, correlationId=null, collection=null, userName=null, timestamp=null,"
                        + " messageType=null, origin=null), authToken=null)",
                connect.toString());
    }

    @Test
    public void testToString() {
        assertEquals("Connect(header=null, authToken=null)", (new Connect()).toString());
    }
}

