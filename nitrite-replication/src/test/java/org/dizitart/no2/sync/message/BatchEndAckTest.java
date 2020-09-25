package org.dizitart.no2.sync.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BatchEndAckTest {
    @Test
    public void testCanEqual() {
        assertFalse((new BatchEndAck()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        BatchEndAck batchEndAck = new BatchEndAck();
        batchEndAck.setHeader(new MessageHeader());
        assertFalse(batchEndAck.equals(new BatchEndAck()));
    }

    @Test
    public void testEquals2() {
        BatchEndAck batchEndAck = new BatchEndAck();
        batchEndAck.setHeader(new MessageHeader());
        assertFalse((new BatchEndAck()).equals(batchEndAck));
    }

    @Test
    public void testEquals3() {
        BatchEndAck batchEndAck = new BatchEndAck();
        assertTrue(batchEndAck.equals(new BatchEndAck()));
    }

    @Test
    public void testEquals4() {
        BatchEndAck batchEndAck = new BatchEndAck();
        batchEndAck.setHeader(new MessageHeader());
        BatchEndAck batchEndAck1 = new BatchEndAck();
        batchEndAck1.setHeader(new MessageHeader());
        assertTrue(batchEndAck.equals(batchEndAck1));
    }

    @Test
    public void testEquals5() {
        assertFalse((new BatchEndAck()).equals("o"));
    }

    @Test
    public void testHashCode() {
        BatchEndAck batchEndAck = new BatchEndAck();
        batchEndAck.setHeader(new MessageHeader());
        assertEquals(64204717, batchEndAck.hashCode());
    }

    @Test
    public void testHashCode2() {
        assertEquals(102, (new BatchEndAck()).hashCode());
    }

    @Test
    public void testSetHeader() {
        BatchEndAck batchEndAck = new BatchEndAck();
        batchEndAck.setHeader(new MessageHeader());
        assertEquals("BatchEndAck(header=MessageHeader(id=null, correlationId=null, collection=null, userName=null,"
                + " timestamp=null, messageType=null, origin=null))", batchEndAck.toString());
    }

    @Test
    public void testToString() {
        assertEquals("BatchEndAck(header=null)", (new BatchEndAck()).toString());
    }
}

