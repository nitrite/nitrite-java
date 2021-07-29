package org.dizitart.no2.sync.message;

import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataGateFeedTest {
    @Test
    public void testCanEqual() {
        assertFalse((new DataGateFeed()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        DataGateFeed dataGateFeed = new DataGateFeed();
        dataGateFeed.setHeader(new MessageHeader());
        DataGateFeed dataGateFeed1 = new DataGateFeed();
        dataGateFeed1.setHeader(new MessageHeader());
        assertTrue(dataGateFeed.equals(dataGateFeed1));
    }

    @Test
    public void testEquals2() {
        DataGateFeed dataGateFeed = new DataGateFeed();
        assertTrue(dataGateFeed.equals(new DataGateFeed()));
    }

    @Test
    public void testEquals3() {
        DataGateFeed dataGateFeed = new DataGateFeed();
        dataGateFeed.setFeed(new LastWriteWinState());
        DataGateFeed dataGateFeed1 = new DataGateFeed();
        dataGateFeed1.setFeed(new LastWriteWinState());
        assertTrue(dataGateFeed.equals(dataGateFeed1));
    }

    @Test
    public void testEquals4() {
        assertFalse((new DataGateFeed()).equals("o"));
    }

    @Test
    public void testEquals5() {
        DataGateFeed dataGateFeed = new DataGateFeed();
        dataGateFeed.setFeed(new LastWriteWinState());
        assertFalse((new DataGateFeed()).equals(dataGateFeed));
    }

    @Test
    public void testEquals6() {
        DataGateFeed dataGateFeed = new DataGateFeed();
        dataGateFeed.setFeed(new LastWriteWinState());
        assertFalse(dataGateFeed.equals(new DataGateFeed()));
    }

    @Test
    public void testEquals7() {
        DataGateFeed dataGateFeed = new DataGateFeed();
        dataGateFeed.setHeader(new MessageHeader());
        assertFalse((new DataGateFeed()).equals(dataGateFeed));
    }

    @Test
    public void testEquals8() {
        DataGateFeed dataGateFeed = new DataGateFeed();
        dataGateFeed.setHeader(new MessageHeader());
        assertFalse(dataGateFeed.equals(new DataGateFeed()));
    }

    @Test
    public void testSetFeed() {
        DataGateFeed dataGateFeed = new DataGateFeed();
        dataGateFeed.setFeed(new LastWriteWinState());
        assertEquals("DataGateFeed(header=null, feed=LastWriteWinState(changeSet=[], tombstoneMap={}))",
                dataGateFeed.toString());
    }

    @Test
    public void testSetHeader() {
        DataGateFeed dataGateFeed = new DataGateFeed();
        dataGateFeed.setHeader(new MessageHeader());
        assertEquals("DataGateFeed(header=MessageHeader(id=null, correlationId=null, collection=null, userName=null,"
                + " timestamp=null, messageType=null, origin=null), feed=null)", dataGateFeed.toString());
    }

    @Test
    public void testToString() {
        assertEquals("DataGateFeed(header=null, feed=null)", (new DataGateFeed()).toString());
    }
}

