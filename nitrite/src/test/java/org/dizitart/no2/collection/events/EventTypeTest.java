package org.dizitart.no2.collection.events;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EventTypeTest {
    @Test
    public void testValueOf() {
        assertEquals(EventType.IndexEnd, EventType.valueOf("IndexEnd"));
    }

    @Test
    public void testValues() {
        assertEquals(5, EventType.values().length);
    }
}

