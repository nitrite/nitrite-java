package org.dizitart.no2.collection.events;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventTypeTest {
    @Test
    public void testValueOf() {
        assertEquals(EventType.IndexEnd, EventType.valueOf("IndexEnd"));
        assertEquals(EventType.IndexStart, EventType.valueOf("IndexStart"));
        assertEquals(EventType.Insert, EventType.valueOf("Insert"));
        assertEquals(EventType.Remove, EventType.valueOf("Remove"));
        assertEquals(EventType.Update, EventType.valueOf("Update"));
    }

    @Test
    public void testValues() {
        assertEquals(5, EventType.values().length);
    }
}

