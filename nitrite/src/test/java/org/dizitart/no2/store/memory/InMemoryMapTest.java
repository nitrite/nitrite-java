package org.dizitart.no2.store.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.mutable.MutableByte;
import org.junit.Test;

public class InMemoryMapTest {
    @Test
    public void testConstructor() {
        InMemoryMap<Object, Object> actualInMemoryMap = new InMemoryMap<Object, Object>("mapName", null);
        assertEquals("mapName", actualInMemoryMap.getName());
        assertEquals(0L, actualInMemoryMap.size());
        assertNull(actualInMemoryMap.getStore());
    }

    @Test
    public void testContainsKey() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put(null, "value");
        assertFalse(inMemoryMap.containsKey("key"));
    }

    @Test
    public void testContainsKey2() {
        assertFalse((new InMemoryMap<Object, Object>("mapName", null)).containsKey("key"));
    }

    @Test
    public void testContainsKey3() {
        assertFalse((new InMemoryMap<Object, Object>("mapName", null)).containsKey(null));
    }

    @Test
    public void testContainsKey4() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put("key", "value");
        assertTrue(inMemoryMap.containsKey("key"));
    }

    @Test
    public void testGet() {
        assertNull((new InMemoryMap<Object, Object>("mapName", null)).get("key"));
    }

    @Test
    public void testGet2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put("key", "value");
        assertEquals("value", inMemoryMap.get("key"));
    }

    @Test
    public void testValues() {
        assertTrue((new InMemoryMap<Object, Object>("mapName", null)).values().isEmpty());
    }

    @Test
    public void testRemove() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>("", null);
        assertNull(inMemoryMap.remove("key"));
        assertEquals(0L, inMemoryMap.size());
    }

    @Test
    public void testRemove2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>("", null);
        assertNull(inMemoryMap.remove(null));
        assertEquals(0L, inMemoryMap.size());
    }

    @Test
    public void testRemove3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>("", null);
        inMemoryMap.put(new MutableByte(), "value");
        assertNull(inMemoryMap.remove(0));
        assertFalse(inMemoryMap.isEmpty());
    }

    @Test
    public void testKeySet() {
        assertTrue((new InMemoryMap<Object, Object>("mapName", null)).keySet().isEmpty());
    }

    @Test
    public void testPut() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put(null, "value");
        assertEquals(1L, inMemoryMap.size());
    }

    @Test
    public void testPut2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put("key", "value");
        assertFalse(inMemoryMap.isEmpty());
    }

    @Test
    public void testPut3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put("key", 1);
        assertFalse(inMemoryMap.isEmpty());
    }

    @Test
    public void testSize() {
        assertEquals(0L, (new InMemoryMap<Object, Object>("mapName", null)).size());
    }

    @Test
    public void testPutIfAbsent() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        assertNull(inMemoryMap.putIfAbsent(null, "value"));
        assertEquals(1L, inMemoryMap.size());
    }

    @Test
    public void testPutIfAbsent2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        assertNull(inMemoryMap.putIfAbsent("key", "value"));
        assertFalse(inMemoryMap.isEmpty());
    }

    @Test
    public void testEntries() {
        assertTrue((new InMemoryMap<Object, Object>("mapName", null)).entries().isEmpty());
    }

    @Test
    public void testHigherKey() {
        assertNull((new InMemoryMap<Object, Object>("mapName", null)).higherKey("key"));
        assertNull((new InMemoryMap<Object, Object>("mapName", null)).higherKey(null));
    }

    @Test
    public void testHigherKey2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put("key", "value");
        assertNull(inMemoryMap.higherKey("key"));
    }

    @Test
    public void testCeilingKey() {
        assertNull((new InMemoryMap<Object, Object>("mapName", null)).ceilingKey("key"));
        assertNull((new InMemoryMap<Object, Object>("mapName", null)).ceilingKey(null));
    }

    @Test
    public void testCeilingKey2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put("key", "value");
        assertEquals("key", inMemoryMap.ceilingKey("key"));
    }

    @Test
    public void testCeilingKey3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put(null, "value");
        assertNull(inMemoryMap.ceilingKey("key"));
    }

    @Test
    public void testLowerKey() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put("key", "value");
        assertNull(inMemoryMap.lowerKey("key"));
    }

    @Test
    public void testLowerKey2() {
        assertNull((new InMemoryMap<Object, Object>("mapName", null)).lowerKey(null));
    }

    @Test
    public void testLowerKey3() {
        assertNull((new InMemoryMap<Object, Object>("mapName", null)).lowerKey("key"));
    }

    @Test
    public void testFloorKey() {
        assertNull((new InMemoryMap<Object, Object>("mapName", null)).floorKey("key"));
        assertNull((new InMemoryMap<Object, Object>("mapName", null)).floorKey(null));
    }

    @Test
    public void testFloorKey2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>(null, null);
        inMemoryMap.put("key", "value");
        assertEquals("key", inMemoryMap.floorKey("key"));
    }

    @Test
    public void testIsEmpty() {
        assertTrue((new InMemoryMap<Object, Object>("mapName", null)).isEmpty());
    }

    @Test
    public void testIsEmpty2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<Object, Object>("", null);
        inMemoryMap.put("key", "value");
        assertFalse(inMemoryMap.isEmpty());
    }
}

