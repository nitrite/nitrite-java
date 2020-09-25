package org.dizitart.no2.collection.events;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CollectionEventInfoTest {
    @Test
    public void testConstructor() {
        assertEquals(EventType.Insert, (new CollectionEventInfo<>(EventType.Insert)).getEventType());
    }
}

