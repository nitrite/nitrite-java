package org.dizitart.no2.sync.crdt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeltaStatesTest {
    @Test
    public void testConstructor() {
        assertEquals("DeltaStates(changeSet=[], tombstoneMap={})", (new DeltaStates()).toString());
    }
}

