package org.dizitart.no2.sync;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TimeSpanTest {
    @Test
    public void testCanEqual() {
        assertFalse((new TimeSpan(10L, TimeUnit.NANOSECONDS)).canEqual("other"));
    }

    @Test
    public void testConstructor() {
        TimeSpan actualTimeSpan = new TimeSpan(10L, TimeUnit.NANOSECONDS);
        assertEquals("TimeSpan(time=10, timeUnit=NANOSECONDS)", actualTimeSpan.toString());
        assertEquals(10L, actualTimeSpan.getTime());
    }

    @Test
    public void testEquals() {
        TimeSpan timeSpan = new TimeSpan(10L, TimeUnit.MICROSECONDS);
        assertFalse(timeSpan.equals(new TimeSpan(10L, TimeUnit.NANOSECONDS)));
    }

    @Test
    public void testEquals2() {
        assertFalse((new TimeSpan(10L, TimeUnit.NANOSECONDS)).equals("o"));
    }

    @Test
    public void testEquals3() {
        TimeSpan timeSpan = new TimeSpan(10L, null);
        assertFalse(timeSpan.equals(new TimeSpan(10L, TimeUnit.NANOSECONDS)));
    }

    @Test
    public void testEquals4() {
        TimeSpan timeSpan = new TimeSpan(10L, null);
        assertTrue(timeSpan.equals(new TimeSpan(10L, null)));
    }

    @Test
    public void testEquals5() {
        TimeSpan timeSpan = new TimeSpan(10L, TimeUnit.NANOSECONDS);
        assertTrue(timeSpan.equals(new TimeSpan(10L, TimeUnit.NANOSECONDS)));
    }

    @Test
    public void testEquals6() {
        TimeSpan timeSpan = new TimeSpan(0L, TimeUnit.NANOSECONDS);
        assertFalse(timeSpan.equals(new TimeSpan(10L, TimeUnit.NANOSECONDS)));
    }

    @Test
    public void testSetTime() {
        TimeSpan timeSpan = new TimeSpan(10L, TimeUnit.NANOSECONDS);
        timeSpan.setTime(10L);
        assertEquals(10L, timeSpan.getTime());
    }

    @Test
    public void testSetTimeUnit() {
        TimeSpan timeSpan = new TimeSpan(10L, TimeUnit.NANOSECONDS);
        timeSpan.setTimeUnit(TimeUnit.NANOSECONDS);
        assertEquals("TimeSpan(time=10, timeUnit=NANOSECONDS)", timeSpan.toString());
    }

    @Test
    public void testToString() {
        assertEquals("TimeSpan(time=10, timeUnit=NANOSECONDS)", (new TimeSpan(10L, TimeUnit.NANOSECONDS)).toString());
    }
}

