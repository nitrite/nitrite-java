package org.dizitart.no2.collection.events;

import org.junit.Assert;
import org.junit.Test;

public class CollectionEventListenerTest {
    @Test
    public void testOnEvent() {
        CollectionEventListener collectionEventListener = Assert::assertNull;
        collectionEventListener.onEvent(null);
    }
}

