package org.dizitart.no2.sync.message;

import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class BatchChangeContinueTest {
    @Test
    public void testCanEqual() {
        assertFalse((new BatchChangeContinue()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setHeader(new MessageHeader());
        assertFalse(batchChangeContinue.equals(new BatchChangeContinue()));
    }

    @Test
    public void testEquals10() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setDebounce(0);
        assertFalse((new BatchChangeContinue()).equals(batchChangeContinue));
    }

    @Test
    public void testEquals2() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setDebounce(0);
        assertFalse(batchChangeContinue.equals(new BatchChangeContinue()));
    }

    @Test
    public void testEquals3() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setHeader(new MessageHeader());
        assertFalse((new BatchChangeContinue()).equals(batchChangeContinue));
    }

    @Test
    public void testEquals4() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setBatchSize(3);
        assertFalse(batchChangeContinue.equals(new BatchChangeContinue()));
    }

    @Test
    public void testEquals5() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        assertTrue(batchChangeContinue.equals(new BatchChangeContinue()));
    }

    @Test
    public void testEquals6() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setFeed(new LastWriteWinState());
        assertFalse(batchChangeContinue.equals(new BatchChangeContinue()));
    }

    @Test
    public void testEquals7() {
        assertFalse((new BatchChangeContinue()).equals("o"));
    }

    @Test
    public void testEquals8() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setFeed(new LastWriteWinState());
        assertFalse((new BatchChangeContinue()).equals(batchChangeContinue));
    }

    @Test
    public void testEquals9() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setBatchSize(3);
        assertFalse((new BatchChangeContinue()).equals(batchChangeContinue));
    }

    @Test
    public void testSetBatchSize() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setBatchSize(3);
        assertEquals(3, batchChangeContinue.getBatchSize().intValue());
    }

    @Test
    public void testSetDebounce() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setDebounce(1);
        assertEquals(1, batchChangeContinue.getDebounce().intValue());
    }

    @Test
    public void testSetFeed() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setFeed(new LastWriteWinState());
        assertEquals("BatchChangeContinue(header=null, feed=LastWriteWinState(changes=[], tombstones={}), batchSize=null,"
            + " debounce=null)", batchChangeContinue.toString());
    }

    @Test
    public void testSetHeader() {
        BatchChangeContinue batchChangeContinue = new BatchChangeContinue();
        batchChangeContinue.setHeader(new MessageHeader());
        assertEquals(
            "BatchChangeContinue(header=MessageHeader(id=null, correlationId=null, collection=null, userName=null,"
                + " timestamp=null, messageType=null, origin=null), feed=null, batchSize=null, debounce=null)",
            batchChangeContinue.toString());
    }

    @Test
    public void testToString() {
        assertEquals("BatchChangeContinue(header=null, feed=null, batchSize=null, debounce=null)",
            (new BatchChangeContinue()).toString());
    }
}

