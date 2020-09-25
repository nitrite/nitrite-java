package org.dizitart.no2.sync.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReplicationEventTypeTest {
    @Test
    public void testValueOf() {
        assertEquals(ReplicationEventType.Error, ReplicationEventType.valueOf("Error"));
    }

    @Test
    public void testValues() {
        assertEquals(3, ReplicationEventType.values().length);
    }
}

