package org.dizitart.no2.collection.events;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class EventAwareTest {
    @Test
    public void testSubscribe() {
        EventAware eventAware = new EventAware() {
            @Override
            public String subscribe(CollectionEventListener listener) {
                assertNull(listener);
                return null;
            }

            @Override
            public void unsubscribe(String listener) {
                // do nothing
            }
        };
        eventAware.subscribe(null);
    }

    @Test
    public void testUnsubscribe() {
        EventAware eventAware = new EventAware() {
            @Override
            public String subscribe(CollectionEventListener listener) {
                return null;
            }

            @Override
            public void unsubscribe(String listener) {
                assertNull(listener);
            }
        };
        eventAware.unsubscribe(null);
    }
}

