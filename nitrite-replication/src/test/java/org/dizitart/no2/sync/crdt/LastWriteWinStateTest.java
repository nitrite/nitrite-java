package org.dizitart.no2.sync.crdt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LastWriteWinStateTest {
    @Test
    public void testConstructor() {
        assertEquals("LastWriteWinState(changes=[], tombstones={})", (new LastWriteWinState()).toString());
    }
}

