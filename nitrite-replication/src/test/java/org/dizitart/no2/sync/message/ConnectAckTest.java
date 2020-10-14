package org.dizitart.no2.sync.message;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConnectAckTest {
    @Test
    public void testCanEqual() {
        assertFalse((new ConnectAck()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        ConnectAck connectAck = new ConnectAck();
        connectAck.setTombstoneTtl(0L);
        assertFalse(connectAck.equals(new ConnectAck()));
    }

    @Test
    public void testEquals2() {
        ConnectAck connectAck = new ConnectAck();
        connectAck.setTombstoneTtl(0L);
        assertFalse((new ConnectAck()).equals(connectAck));
    }

    @Test
    public void testEquals3() {
        ConnectAck connectAck = new ConnectAck();
        connectAck.setHeader(new MessageHeader());
        ConnectAck connectAck1 = new ConnectAck();
        connectAck1.setHeader(new MessageHeader());
        assertTrue(connectAck.equals(connectAck1));
    }

    @Test
    public void testEquals4() {
        ConnectAck connectAck = new ConnectAck();
        assertTrue(connectAck.equals(new ConnectAck()));
    }

    @Test
    public void testEquals5() {
        ConnectAck connectAck = new ConnectAck();
        connectAck.setTombstoneTtl(1L);
        ConnectAck connectAck1 = new ConnectAck();
        connectAck1.setTombstoneTtl(1L);
        assertTrue(connectAck.equals(connectAck1));
    }

    @Test
    public void testEquals6() {
        ConnectAck connectAck = new ConnectAck();
        connectAck.setHeader(new MessageHeader());
        assertFalse((new ConnectAck()).equals(connectAck));
    }

    @Test
    public void testEquals7() {
        ConnectAck connectAck = new ConnectAck();
        connectAck.setHeader(new MessageHeader());
        assertFalse(connectAck.equals(new ConnectAck()));
    }

    @Test
    public void testEquals8() {
        assertFalse((new ConnectAck()).equals("o"));
    }

    @Test
    public void testSetHeader() {
        ConnectAck connectAck = new ConnectAck();
        connectAck.setHeader(new MessageHeader());
        assertEquals(
            "ConnectAck(header=MessageHeader(id=null, correlationId=null, collection=null, userName=null, timestamp=null,"
                + " messageType=null, origin=null), tombstoneTtl=null)",
            connectAck.toString());
    }

    @Test
    public void testSetTombstoneTtl() {
        ConnectAck connectAck = new ConnectAck();
        connectAck.setTombstoneTtl(1L);
        assertEquals(1L, connectAck.getTombstoneTtl().longValue());
    }

    @Test
    public void testToString() {
        assertEquals("ConnectAck(header=null, tombstoneTtl=null)", (new ConnectAck()).toString());
    }
}

