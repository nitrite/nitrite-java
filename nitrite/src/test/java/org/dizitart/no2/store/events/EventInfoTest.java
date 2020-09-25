package org.dizitart.no2.store.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.dizitart.no2.NitriteConfig;
import org.junit.Test;

public class EventInfoTest {
    @Test
    public void testCanEqual() {
        assertFalse((new EventInfo()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        assertFalse((new EventInfo()).equals("o"));
    }

    @Test
    public void testEquals2() {
        EventInfo o = new EventInfo(StoreEvents.Opened, new NitriteConfig());
        assertFalse((new EventInfo()).equals(o));
    }

    @Test
    public void testEquals3() {
        EventInfo eventInfo = new EventInfo(StoreEvents.Opened, new NitriteConfig());
        assertFalse(eventInfo.equals(new EventInfo()));
    }

    @Test
    public void testEquals4() {
        EventInfo eventInfo = new EventInfo();
        assertTrue(eventInfo.equals(new EventInfo()));
    }

    @Test
    public void testEquals5() {
        EventInfo eventInfo = new EventInfo(StoreEvents.Opened, new NitriteConfig());
        assertFalse(eventInfo.equals(new EventInfo(StoreEvents.Opened, new NitriteConfig())));
    }

    @Test
    public void testEquals6() {
        EventInfo o = new EventInfo(null, new NitriteConfig());
        assertFalse((new EventInfo()).equals(o));
    }

    @Test
    public void testHashCode() {
        assertEquals(6061, (new EventInfo()).hashCode());
    }

    @Test
    public void testSetEvent() {
        EventInfo eventInfo = new EventInfo();
        eventInfo.setEvent(StoreEvents.Opened);
        assertEquals(StoreEvents.Opened, eventInfo.getEvent());
    }

    @Test
    public void testSetNitriteConfig() {
        EventInfo eventInfo = new EventInfo();
        NitriteConfig nitriteConfig = new NitriteConfig();
        eventInfo.setNitriteConfig(nitriteConfig);
        assertSame(nitriteConfig, eventInfo.getNitriteConfig());
    }

    @Test
    public void testToString() {
        assertEquals("EventInfo(event=null, nitriteConfig=null)", (new EventInfo()).toString());
    }
}

