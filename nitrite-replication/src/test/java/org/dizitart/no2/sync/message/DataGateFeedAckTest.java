package org.dizitart.no2.sync.message;

import org.junit.Test;

import static org.junit.Assert.*;

public class DataGateFeedAckTest {
    @Test
    public void testCanEqual() {
        assertFalse((new DataGateFeedAck()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        assertFalse((new DataGateFeedAck()).equals("o"));
    }

    @Test
    public void testEquals2() {
        DataGateFeedAck dataGateFeedAck = new DataGateFeedAck();
        assertTrue(dataGateFeedAck.equals(new DataGateFeedAck()));
    }

    @Test
    public void testEquals3() {
        DataGateFeedAck dataGateFeedAck = new DataGateFeedAck();
        dataGateFeedAck.setHeader(new MessageHeader());
        assertFalse((new DataGateFeedAck()).equals(dataGateFeedAck));
    }

    @Test
    public void testEquals4() {
        DataGateFeedAck dataGateFeedAck = new DataGateFeedAck();
        dataGateFeedAck.setReceipt(new Receipt());
        assertFalse((new DataGateFeedAck()).equals(dataGateFeedAck));
    }

    @Test
    public void testEquals5() {
        DataGateFeedAck dataGateFeedAck = new DataGateFeedAck();
        dataGateFeedAck.setHeader(new MessageHeader());
        DataGateFeedAck dataGateFeedAck1 = new DataGateFeedAck();
        dataGateFeedAck1.setHeader(new MessageHeader());
        assertTrue(dataGateFeedAck.equals(dataGateFeedAck1));
    }

    @Test
    public void testEquals6() {
        DataGateFeedAck dataGateFeedAck = new DataGateFeedAck();
        dataGateFeedAck.setHeader(new MessageHeader());
        assertFalse(dataGateFeedAck.equals(new DataGateFeedAck()));
    }

    @Test
    public void testEquals7() {
        DataGateFeedAck dataGateFeedAck = new DataGateFeedAck();
        dataGateFeedAck.setReceipt(new Receipt());
        DataGateFeedAck dataGateFeedAck1 = new DataGateFeedAck();
        dataGateFeedAck1.setReceipt(new Receipt());
        assertTrue(dataGateFeedAck.equals(dataGateFeedAck1));
    }

    @Test
    public void testEquals8() {
        DataGateFeedAck dataGateFeedAck = new DataGateFeedAck();
        dataGateFeedAck.setReceipt(new Receipt());
        assertFalse(dataGateFeedAck.equals(new DataGateFeedAck()));
    }

    @Test
    public void testSetHeader() {
        DataGateFeedAck dataGateFeedAck = new DataGateFeedAck();
        dataGateFeedAck.setHeader(new MessageHeader());
        assertEquals("DataGateFeedAck(header=MessageHeader(id=null, correlationId=null, collection=null, userName=null,"
            + " timestamp=null, messageType=null, origin=null), receipt=null)", dataGateFeedAck.toString());
    }

    @Test
    public void testSetReceipt() {
        DataGateFeedAck dataGateFeedAck = new DataGateFeedAck();
        dataGateFeedAck.setReceipt(new Receipt());
        assertEquals("DataGateFeedAck(header=null, receipt=Receipt(added=[], removed=[]))", dataGateFeedAck.toString());
    }

    @Test
    public void testToString() {
        assertEquals("DataGateFeedAck(header=null, receipt=null)", (new DataGateFeedAck()).toString());
    }
}

