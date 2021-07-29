package org.dizitart.no2.sync.message;

import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.junit.Test;

import static org.junit.Assert.*;

public class BatchChangeStartTest {
    @Test
    public void testCanEqual() {
        assertFalse((new BatchChangeStart()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setDebounce(0);
        assertFalse((new BatchChangeStart()).equals(batchChangeStart));
    }

    @Test
    public void testEquals10() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setFeed(new LastWriteWinState());
        assertFalse(batchChangeStart.equals(new BatchChangeStart()));
    }

    @Test
    public void testEquals2() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setHeader(new MessageHeader());
        assertFalse(batchChangeStart.equals(new BatchChangeStart()));
    }

    @Test
    public void testEquals3() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setFeed(new LastWriteWinState());
        assertFalse((new BatchChangeStart()).equals(batchChangeStart));
    }

    @Test
    public void testEquals4() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setBatchSize(3);
        assertFalse(batchChangeStart.equals(new BatchChangeStart()));
    }

    @Test
    public void testEquals5() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setHeader(new MessageHeader());
        assertFalse((new BatchChangeStart()).equals(batchChangeStart));
    }

    @Test
    public void testEquals6() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setBatchSize(3);
        assertFalse((new BatchChangeStart()).equals(batchChangeStart));
    }

    @Test
    public void testEquals7() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setDebounce(0);
        assertFalse(batchChangeStart.equals(new BatchChangeStart()));
    }

    @Test
    public void testEquals8() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        assertTrue(batchChangeStart.equals(new BatchChangeStart()));
    }

    @Test
    public void testEquals9() {
        assertFalse((new BatchChangeStart()).equals("o"));
    }

    @Test
    public void testSetBatchSize() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setBatchSize(3);
        assertEquals(3, batchChangeStart.getBatchSize().intValue());
    }

    @Test
    public void testSetDebounce() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setDebounce(1);
        assertEquals(1, batchChangeStart.getDebounce().intValue());
    }

    @Test
    public void testSetFeed() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setFeed(new LastWriteWinState());
        assertEquals("BatchChangeStart(super=TimeBoundMessage(startTime=null, endTime=null), header=null, " +
            "batchSize=null, debounce=null, feed=LastWriteWinState(changeSet=[], tombstoneMap={}))",
            batchChangeStart.toString());
    }

    @Test
    public void testSetHeader() {
        BatchChangeStart batchChangeStart = new BatchChangeStart();
        batchChangeStart.setHeader(new MessageHeader());
        assertEquals(
                "BatchChangeStart(super=TimeBoundMessage(startTime=null, endTime=null), header=MessageHeader" +
                    "(id=null, correlationId=null, collection=null, userName=null, timestamp=null, messageType=null, " +
                    "origin=null), batchSize=null, debounce=null, feed=null)",
                batchChangeStart.toString());
    }

    @Test
    public void testToString() {
        assertEquals("BatchChangeStart(super=TimeBoundMessage(startTime=null, endTime=null), header=null, " +
                "batchSize=null, debounce=null, feed=null)",
                (new BatchChangeStart()).toString());
    }
}

