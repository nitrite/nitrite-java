package org.dizitart.no2.store.events;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StoreEventsTest {
    @Test
    public void testValueOf() {
        assertEquals(StoreEvents.Closed, StoreEvents.valueOf("Closed"));
    }

    @Test
    public void testValues() {
        assertEquals(4, StoreEvents.values().length);
    }
}

