package org.dizitart.no2.collection.meta;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class AttributesTest {
    @Test
    public void testConstructor() {
        Map<String, String> attributes = (new Attributes()).getAttributes();
        assertTrue(attributes instanceof java.util.concurrent.ConcurrentHashMap);
        assertEquals(3, attributes.size());
    }

    @Test
    public void testConstructor2() {
        Map<String, String> attributes = (new Attributes("collection")).getAttributes();
        assertTrue(attributes instanceof java.util.concurrent.ConcurrentHashMap);
        assertEquals(4, attributes.size());
    }

    @Test
    public void testSet() {
        Attributes attributes = new Attributes();
        assertSame(attributes, attributes.set("key", "value"));
    }

    @Test
    public void testGet() {
        assertNull((new Attributes()).get("key"));
    }

    @Test
    public void testHasKey() {
        assertFalse((new Attributes()).hasKey("key"));
    }
}

