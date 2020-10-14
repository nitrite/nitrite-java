package org.dizitart.no2.sync.message;

import org.junit.Test;

import static org.junit.Assert.*;

public class BatchAckTest {
    @Test
    public void testCanEqual() {
        assertFalse((new BatchAck()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        BatchAck batchAck = new BatchAck();
        batchAck.setHeader(new MessageHeader());
        assertFalse((new BatchAck()).equals(batchAck));
    }

    @Test
    public void testEquals2() {
        BatchAck batchAck = new BatchAck();
        batchAck.setHeader(new MessageHeader());
        assertFalse(batchAck.equals(new BatchAck()));
    }

    @Test
    public void testEquals3() {
        BatchAck batchAck = new BatchAck();
        batchAck.setReceipt(new Receipt());
        BatchAck batchAck1 = new BatchAck();
        batchAck1.setReceipt(new Receipt());
        assertTrue(batchAck.equals(batchAck1));
    }

    @Test
    public void testEquals4() {
        BatchAck batchAck = new BatchAck();
        batchAck.setReceipt(new Receipt());
        assertFalse((new BatchAck()).equals(batchAck));
    }

    @Test
    public void testEquals5() {
        BatchAck batchAck = new BatchAck();
        batchAck.setHeader(new MessageHeader());
        BatchAck batchAck1 = new BatchAck();
        batchAck1.setHeader(new MessageHeader());
        assertTrue(batchAck.equals(batchAck1));
    }

    @Test
    public void testEquals6() {
        BatchAck batchAck = new BatchAck();
        assertTrue(batchAck.equals(new BatchAck()));
    }

    @Test
    public void testEquals7() {
        BatchAck batchAck = new BatchAck();
        batchAck.setReceipt(new Receipt());
        assertFalse(batchAck.equals(new BatchAck()));
    }

    @Test
    public void testEquals8() {
        assertFalse((new BatchAck()).equals("o"));
    }

    @Test
    public void testSetHeader() {
        BatchAck batchAck = new BatchAck();
        batchAck.setHeader(new MessageHeader());
        assertEquals(
                "BatchAck(header=MessageHeader(id=null, correlationId=null, collection=null, userName=null, timestamp=null,"
                        + " messageType=null, origin=null), receipt=null)",
                batchAck.toString());
    }

    @Test
    public void testSetReceipt() {
        BatchAck batchAck = new BatchAck();
        batchAck.setReceipt(new Receipt());
        assertEquals("BatchAck(header=null, receipt=Receipt(added=[], removed=[]))", batchAck.toString());
    }

    @Test
    public void testToString() {
        assertEquals("BatchAck(header=null, receipt=null)", (new BatchAck()).toString());
    }
}

