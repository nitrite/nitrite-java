package org.dizitart.no2.sync.message;

import org.junit.Test;

import static org.junit.Assert.*;

public class DisconnectTest {
    @Test
    public void testCanEqual() {
        assertFalse((new Disconnect()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        Disconnect disconnect = new Disconnect();
        disconnect.setHeader(new MessageHeader());
        assertFalse(disconnect.equals(new Disconnect()));
    }

    @Test
    public void testEquals2() {
        Disconnect disconnect = new Disconnect();
        disconnect.setHeader(new MessageHeader());
        Disconnect disconnect1 = new Disconnect();
        disconnect1.setHeader(new MessageHeader());
        assertTrue(disconnect.equals(disconnect1));
    }

    @Test
    public void testEquals3() {
        Disconnect disconnect = new Disconnect();
        assertTrue(disconnect.equals(new Disconnect()));
    }

    @Test
    public void testEquals4() {
        Disconnect disconnect = new Disconnect();
        disconnect.setHeader(new MessageHeader());
        assertFalse((new Disconnect()).equals(disconnect));
    }

    @Test
    public void testEquals5() {
        assertFalse((new Disconnect()).equals("o"));
    }

    @Test
    public void testSetHeader() {
        Disconnect disconnect = new Disconnect();
        disconnect.setHeader(new MessageHeader());
        assertEquals(
                "Disconnect(header=MessageHeader(id=null, correlationId=null, collection=null, userName=null, timestamp=null,"
                        + " messageType=null, origin=null))",
                disconnect.toString());
    }

    @Test
    public void testToString() {
        assertEquals("Disconnect(header=null)", (new Disconnect()).toString());
    }
}

