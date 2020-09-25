package org.dizitart.no2.sync.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
    public void testHashCode() {
        Disconnect disconnect = new Disconnect();
        disconnect.setHeader(new MessageHeader());
        assertEquals(64204717, disconnect.hashCode());
    }

    @Test
    public void testHashCode2() {
        assertEquals(102, (new Disconnect()).hashCode());
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

