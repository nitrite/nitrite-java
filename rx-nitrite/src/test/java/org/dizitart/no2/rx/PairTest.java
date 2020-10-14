package org.dizitart.no2.rx;

import org.junit.Test;

import static org.junit.Assert.*;

public class PairTest {
    @Test
    public void testCanEqual() {
        assertFalse((new Pair<Object, Object>("key", "value")).canEqual("other"));
    }

    @Test
    public void testEquals() {
        Pair<Object, Object> pair = new Pair<>(new Pair("key", "value"), "value");
        assertFalse(pair.equals(new Pair("key", "value")));
    }

    @Test
    public void testEquals2() {
        Pair<Object, Object> pair = new Pair<>(null, "value");
        assertFalse(pair.equals(new Pair("key", "value")));
    }

    @Test
    public void testEquals3() {
        Pair<Object, Object> pair = new Pair<>("key", "value");
        assertTrue(pair.equals(new Pair("key", "value")));
    }

    @Test
    public void testEquals4() {
        Pair<Object, Object> pair = new Pair<>("key", null);
        assertFalse(pair.equals(new Pair("key", "value")));
    }

    @Test
    public void testEquals5() {
        assertFalse((new Pair<Object, Object>("key", "value")).equals("o"));
    }

    @Test
    public void testEquals6() {
        Pair<Object, Object> pair = new Pair<>("key", null);
        assertTrue(pair.equals(new Pair("key", null)));
    }

    @Test
    public void testEquals7() {
        Pair<Object, Object> pair = new Pair<>(null, "value");
        assertTrue(pair.equals(new Pair(null, "value")));
    }

    @Test
    public void testEquals8() {
        Pair<Object, Object> pair = new Pair<>("key", new Pair("key", "value"));
        assertFalse(pair.equals(new Pair("key", "value")));
    }

    @Test
    public void testEquals9() {
        Pair<Object, Object> pair = new Pair<>("key", 42);
        assertFalse(pair.equals(new Pair("key", "value")));
    }

    @Test
    public void testSetKey() {
        Pair<Object, Object> pair = new Pair<>("key", "value");
        pair.setKey("key");
        assertEquals("Pair(key=key, value=value)", pair.toString());
    }

    @Test
    public void testSetValue() {
        Pair<Object, Object> pair = new Pair<>("key", "value");
        pair.setValue("value");
        assertEquals("Pair(key=key, value=value)", pair.toString());
    }

    @Test
    public void testToString() {
        assertEquals("Pair(key=key, value=value)", (new Pair<Object, Object>("key", "value")).toString());
    }
}

