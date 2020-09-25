package org.dizitart.no2.collection.events;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class EventAwareTest {
    @Test
    public void testSubscribe() {
        EventAware eventAware = new EventAware() {
            @Override
            public void subscribe(CollectionEventListener listener) {
                assertNull(listener);
            }

            @Override
            public void unsubscribe(CollectionEventListener listener) {

            }
        };
        eventAware.subscribe(null);
    }

    @Test
    public void testUnsubscribe() {
        EventAware eventAware = new EventAware() {
            @Override
            public void subscribe(CollectionEventListener listener) {
            }

            @Override
            public void unsubscribe(CollectionEventListener listener) {
                assertNull(listener);
            }
        };
        eventAware.unsubscribe(null);
    }
}

