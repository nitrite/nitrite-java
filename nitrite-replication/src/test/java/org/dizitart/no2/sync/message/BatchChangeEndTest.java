package org.dizitart.no2.sync.message;

import org.junit.Test;

import static org.junit.Assert.*;

public class BatchChangeEndTest {
    @Test
    public void testCanEqual() {
        assertFalse((new BatchChangeEnd()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setLastSynced(0L);
        assertFalse((new BatchChangeEnd()).equals(batchChangeEnd));
    }

    @Test
    public void testEquals10() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setLastSynced(0L);
        assertFalse(batchChangeEnd.equals(new BatchChangeEnd()));
    }

    @Test
    public void testEquals2() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setDebounce(0);
        assertFalse((new BatchChangeEnd()).equals(batchChangeEnd));
    }

    @Test
    public void testEquals3() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        assertTrue(batchChangeEnd.equals(new BatchChangeEnd()));
    }

    @Test
    public void testEquals4() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setHeader(new MessageHeader());
        assertFalse(batchChangeEnd.equals(new BatchChangeEnd()));
    }

    @Test
    public void testEquals5() {
        assertFalse((new BatchChangeEnd()).equals("o"));
    }

    @Test
    public void testEquals6() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setBatchSize(3);
        assertFalse(batchChangeEnd.equals(new BatchChangeEnd()));
    }

    @Test
    public void testEquals7() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setBatchSize(3);
        assertFalse((new BatchChangeEnd()).equals(batchChangeEnd));
    }

    @Test
    public void testEquals8() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setHeader(new MessageHeader());
        assertFalse((new BatchChangeEnd()).equals(batchChangeEnd));
    }

    @Test
    public void testEquals9() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setDebounce(0);
        assertFalse(batchChangeEnd.equals(new BatchChangeEnd()));
    }

    @Test
    public void testSetBatchSize() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setBatchSize(3);
        assertEquals(3, batchChangeEnd.getBatchSize().intValue());
    }

    @Test
    public void testSetDebounce() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setDebounce(1);
        assertEquals(1, batchChangeEnd.getDebounce().intValue());
    }

    @Test
    public void testSetHeader() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setHeader(new MessageHeader());
        assertEquals(
            "BatchChangeEnd(header=MessageHeader(id=null, correlationId=null, collection=null, userName=null,"
                + " timestamp=null, messageType=null, origin=null), lastSynced=null, batchSize=null, debounce=null)",
            batchChangeEnd.toString());
    }

    @Test
    public void testSetLastSynced() {
        BatchChangeEnd batchChangeEnd = new BatchChangeEnd();
        batchChangeEnd.setLastSynced(1L);
        assertEquals(1L, batchChangeEnd.getLastSynced().longValue());
    }

    @Test
    public void testToString() {
        assertEquals("BatchChangeEnd(header=null, lastSynced=null, batchSize=null, debounce=null)",
            (new BatchChangeEnd()).toString());
    }
}

