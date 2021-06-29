package org.dizitart.no2.store.memory;

import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.junit.Assert.*;

public class InMemoryMapTest {

    @Test
    public void testContainsKey() {
        assertFalse((new InMemoryMap<>("Map Name", null)).containsKey("Key"));
    }

    @Test
    public void testContainsKey2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("Key", "Value");
        assertTrue(inMemoryMap.containsKey("Key"));
    }

    @Test
    public void testContainsKey3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("Key", "Value");
        inMemoryMap.put("Key", "Value");
        assertTrue(inMemoryMap.containsKey("Key"));
    }

    @Test
    public void testContainsKey4() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("42", "Value");
        assertFalse(inMemoryMap.containsKey("Key"));
    }

    @Test
    public void testGet() {
        assertNull((new InMemoryMap<>("Map Name", null)).get("Key"));
    }

    @Test
    public void testGet2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("Key", "Value");
        assertEquals("Value", inMemoryMap.get("Key"));
    }

    @Test
    public void testGet3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("42", "Value");
        assertNull(inMemoryMap.get("Key"));
    }

    @Test
    public void testGet4() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put((byte) 'A', "Value");
        assertNull(inMemoryMap.get((byte) 0));
    }

    @Test
    public void testClear() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("Key", "Value");
        inMemoryMap.clear();
        assertEquals(0L, inMemoryMap.size());
    }

    @Test
    public void testClear2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("Map Name", new InMemoryStore());
        inMemoryMap.put("Key", "Value");
        inMemoryMap.clear();
        assertEquals(0L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testClear3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("lastModifiedTime", new InMemoryStore());
        inMemoryMap.put("Key", "Value");
        inMemoryMap.clear();
        assertEquals(0L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testClear4() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("Map Name", new InMemoryStore());
        inMemoryMap.clear();
        assertEquals(0L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testClear5() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("owner", new InMemoryStore());
        inMemoryMap.clear();
        assertEquals(0L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testValues() {
        assertTrue((new InMemoryMap<>("Map Name", null)).values().toList().isEmpty());
    }

    @Test
    public void testRemove() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("Key", "Value");
        assertEquals("Value", inMemoryMap.remove("Key"));
        assertEquals(0L, inMemoryMap.size());
    }

    @Test
    public void testRemove2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("Map Name", new InMemoryStore());
        inMemoryMap.put("Key", "Value");
        assertEquals("Value", inMemoryMap.remove("Key"));
        assertEquals(0L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testRemove3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("", "Value");
        assertNull(inMemoryMap.remove("Key"));
        assertEquals(1L, inMemoryMap.size());
    }

    @Test
    public void testRemove4() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("Map Name", new InMemoryStore());
        inMemoryMap.put("Key", "Value");
        assertNull(inMemoryMap.remove("42"));
        assertEquals(1L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testKeys() {
        assertTrue((new InMemoryMap<>("Map Name", null)).keys().toList().isEmpty());
    }

    @Test
    public void testPut() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("Key", "Value");
        inMemoryMap.put("Key", "Value");
        assertEquals(1L, inMemoryMap.size());
    }

    @Test
    public void testPut2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("Map Name", new InMemoryStore());
        inMemoryMap.put("Key", "Value");
        inMemoryMap.put("Key", "Value");
        assertEquals(1L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testPut3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("value cannot be null", "Value");
        inMemoryMap.put("Key", "Value");
        assertEquals(2L, inMemoryMap.size());
    }

    @Test(expected = ValidationException.class)
    public void testPut4() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put((byte) 'A', "Value");
        inMemoryMap.put("Key", null);
    }

    @Test
    public void testPut5() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("Map Name", new InMemoryStore());
        inMemoryMap.put("42", "Value");
        inMemoryMap.put("Key", "Value");
        assertEquals(2L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testSize() {
        assertEquals(0L, (new InMemoryMap<>("Map Name", null)).size());
    }

    @Test
    public void testPutIfAbsent() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("Key", "Value");
        assertEquals("Value", inMemoryMap.putIfAbsent("Key", "Value"));
    }

    @Test
    public void testPutIfAbsent2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("Map Name", new InMemoryStore());
        inMemoryMap.put("Key", "Value");
        assertEquals("Value", inMemoryMap.putIfAbsent("Key", "Value"));
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testPutIfAbsent3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("value cannot be null", "Value");
        assertNull(inMemoryMap.putIfAbsent("Key", "Value"));
        assertEquals(2L, inMemoryMap.size());
    }

    @Test(expected = ValidationException.class)
    public void testPutIfAbsent4() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put((byte) 'A', "Value");
        inMemoryMap.putIfAbsent("Key", null);
    }

    @Test
    public void testPutIfAbsent5() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("Map Name", new InMemoryStore());
        inMemoryMap.put("Key", "Value");
        assertNull(inMemoryMap.putIfAbsent("42", "Value"));
        assertEquals(2L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testEntries() {
        assertTrue((new InMemoryMap<>("Map Name", null)).entries().toList().isEmpty());
    }

    @Test
    public void testReversedEntries() {
        assertTrue((new InMemoryMap<>("Map Name", null)).reversedEntries().toList().isEmpty());
    }

    @Test
    public void testHigherKey() {
        assertNull((new InMemoryMap<>("Map Name", null)).higherKey("Key"));
        assertNull((new InMemoryMap<>("Map Name", null)).higherKey(null));
    }

    @Test
    public void testHigherKey2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>(null, null);
        inMemoryMap.put("Key", "Value");
        assertNull(inMemoryMap.higherKey("Key"));
    }

    @Test
    public void testHigherKey3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>(null, null);
        inMemoryMap.put((byte) 'A', "Value");
        assertEquals('A', ((Byte) inMemoryMap.higherKey(-1)).byteValue());
    }

    @Test
    public void testCeilingKey() {
        assertNull((new InMemoryMap<>("Map Name", null)).ceilingKey("Key"));
        assertNull((new InMemoryMap<>("Map Name", null)).ceilingKey(null));
    }

    @Test
    public void testCeilingKey2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>(null, null);
        inMemoryMap.put("Key", "Value");
        assertEquals("Key", inMemoryMap.ceilingKey("Key"));
    }

    @Test
    public void testCeilingKey3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>(null, null);
        inMemoryMap.put((byte) 'A', "Value");
        assertEquals('A', ((Byte) inMemoryMap.ceilingKey(0)).byteValue());
    }

    @Test
    public void testLowerKey() {
        assertNull((new InMemoryMap<>("Map Name", null)).lowerKey("Key"));
        assertNull((new InMemoryMap<>("Map Name", null)).lowerKey(null));
    }

    @Test
    public void testLowerKey2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>(null, null);
        inMemoryMap.put("Key", "Value");
        assertNull(inMemoryMap.lowerKey("Key"));
    }

    @Test
    public void testLowerKey3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>(null, null);
        inMemoryMap.put((byte) 'A', "Value");
        assertNull(inMemoryMap.lowerKey(10.0f));
    }

    @Test
    public void testFloorKey() {
        assertNull((new InMemoryMap<>("Map Name", null)).floorKey("Key"));
        assertNull((new InMemoryMap<>("Map Name", null)).floorKey(null));
    }

    @Test
    public void testFloorKey2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>(null, null);
        inMemoryMap.put("Key", "Value");
        assertEquals("Key", inMemoryMap.floorKey("Key"));
    }

    @Test
    public void testFloorKey3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>(null, null);
        inMemoryMap.put((byte) 'A', "Value");
        assertNull(inMemoryMap.floorKey(0));
    }

    @Test
    public void testIsEmpty() {
        assertTrue((new InMemoryMap<>("Map Name", null)).isEmpty());
    }

    @Test
    public void testIsEmpty2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("", null);
        inMemoryMap.put("Key", "Value");
        assertFalse(inMemoryMap.isEmpty());
    }

    @Test
    public void testDrop() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("Map Name", new InMemoryStore());
        inMemoryMap.put("Key", "Value");
        inMemoryMap.drop();
        assertEquals(0L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testDrop2() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("42", new InMemoryStore());
        inMemoryMap.put("Key", "Value");
        inMemoryMap.drop();
        assertEquals(0L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testDrop3() {
        InMemoryMap<Object, Object> inMemoryMap = new InMemoryMap<>("Map Name", new InMemoryStore());
        inMemoryMap.drop();
        assertEquals(0L, inMemoryMap.size());
        assertEquals(4, inMemoryMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testClose() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        (new InMemoryMap<>("Map Name", null)).close();
    }

    @Test
    public void testConstructor() {
        InMemoryMap<Object, Object> actualInMemoryMap = new InMemoryMap<>("Map Name", null);
        assertEquals("Map Name", actualInMemoryMap.getName());
        assertNull(actualInMemoryMap.getStore());
    }

    @Test
    public void testConstructor2() {
        InMemoryMap<Object, Object> actualInMemoryMap = new InMemoryMap<>("Map Name", null);
        assertEquals("Map Name", actualInMemoryMap.getName());
        assertEquals(0L, actualInMemoryMap.size());
        assertNull(actualInMemoryMap.getStore());
    }

}

