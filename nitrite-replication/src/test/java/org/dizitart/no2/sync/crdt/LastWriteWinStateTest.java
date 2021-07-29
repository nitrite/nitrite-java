package org.dizitart.no2.sync.crdt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LastWriteWinStateTest {
    @Test
    public void testConstructor() {
        assertEquals("LastWriteWinState(changeSet=[], tombstoneMap={})", (new LastWriteWinState()).toString());
    }
}

