package org.dizitart.no2.collection;

import tools.jackson.databind.introspect.AnnotatedMethodMap;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class NitriteDocumentTest {
    @Test
    public void testPut() {
        assertThrows(InvalidOperationException.class, () -> (new NitriteDocument()).put("", "value"));
    }

    @Test
    public void testPut2() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        Document actualPutResult = nitriteDocument.put("_id", 42);
        assertSame(nitriteDocument, actualPutResult);
        assertEquals(1, actualPutResult.size());
    }

    @Test
    public void testGet() {
        Class type = Object.class;
        assertNull((new NitriteDocument()).get(null, type));
        assertNull((new NitriteDocument()).get("Field"));
        assertNull((new NitriteDocument()).get(null));
        assertNull((new NitriteDocument()).get("java.io.Serializable"));
    }

    @Test
    public void testGet2() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("java.io.Serializable", "Value");
        assertEquals("Value", nitriteDocument.get("java.io.Serializable"));
    }

    @Test
    public void testGet3() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        assertNull(nitriteDocument.get("Field", Object.class));
    }

    @Test
    public void testGet4() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        assertNull(nitriteDocument.get(null, Object.class));
    }

    @Test
    public void testGetId() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.putIfAbsent("_id", 42L);
        assertEquals(42L, nitriteDocument.getId().getIdValue());
    }

    @Test
    public void testGetId2() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("_id", 42);
        assertThrows(InvalidIdException.class, nitriteDocument::getId);
    }

    @Test
    public void testGetFields() {
        assertTrue((new NitriteDocument()).getFields().isEmpty());

        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("foo", "foo");
        Set<String> actualFields = nitriteDocument.getFields();
        assertEquals(1, actualFields.size());
        assertTrue(actualFields.contains("foo"));
    }

    @Test
    public void testGetFields2() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("foo", new NitriteDocument());
        assertEquals(0, nitriteDocument.getFields().size());
    }

    @Test(expected = ValidationException.class)
    public void testGetFields3() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("foo", new AnnotatedMethodMap());
        assertEquals(1, nitriteDocument.getFields().size());
    }

    @Test
    public void testGetFields4() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.putIfAbsent("", new NitriteDocument());
        assertTrue(nitriteDocument.getFields().isEmpty());
    }

    @Test
    public void testGetFields5() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.putIfAbsent("", new ArrayList<String>());
        assertTrue(nitriteDocument.getFields().isEmpty());
    }

    @Test
    public void testGetFields6() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("java.io.Serializable", "Value");
        Set<String> actualFields = nitriteDocument.getFields();
        assertEquals(1, actualFields.size());
        assertTrue(actualFields.contains("java.io.Serializable"));
    }

    @Test
    public void testHasId() {
        assertFalse((new NitriteDocument()).hasId());
    }

    @Test
    public void testHasId2() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.putIfAbsent("_id", "42");
        assertTrue(nitriteDocument.hasId());
    }

    @Test
    public void testClone() {
        assertEquals(0, (new NitriteDocument()).clone().size());
    }

    @Test
    public void testClone2() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("Field", "Value");
        assertEquals(1, nitriteDocument.clone().size());
    }

    @Test
    public void testClone3() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("Field", new NitriteDocument());
        assertEquals(1, nitriteDocument.clone().size());
    }

    @Test
    public void testCloneCopiesNestedContainers() {
        List<Object> tags = new LinkedList<>(Arrays.asList("a", "b"));
        NitriteDocument embedded = new NitriteDocument();
        embedded.put("n", 1);
        List<Document> embeddedList = new ArrayList<>(Collections.singletonList(embedded));
        Map<String, Object> map = new HashMap<>();
        map.put("k", new ArrayList<>(Collections.singletonList("v")));
        byte[] bytes = {1, 2, 3};
        int[] ints = {1, 2, 3};
        Date date = new Date(1000L);
        SortedSet<String> sorted = new TreeSet<>(Comparator.reverseOrder());
        sorted.addAll(Arrays.asList("x", "y"));

        NitriteDocument original = new NitriteDocument();
        original.put("tags", tags);
        original.put("docs", embeddedList);
        original.put("map", map);
        original.put("bytes", bytes);
        original.put("ints", ints);
        original.put("date", date);
        original.put("sorted", sorted);
        original.put("fixed", Collections.unmodifiableList(Arrays.asList("p", "q")));
        original.put("name", "immutable");

        Document clone = original.clone();
        assertEquals(original, clone);

        // nothing mutable is shared
        assertNotSame(tags, clone.get("tags"));
        assertNotSame(embeddedList, clone.get("docs"));
        assertNotSame(embedded, ((List<?>) clone.get("docs")).get(0));
        assertNotSame(map, clone.get("map"));
        assertNotSame(map.get("k"), ((Map<?, ?>) clone.get("map")).get("k"));
        assertNotSame(bytes, clone.get("bytes"));
        assertNotSame(ints, clone.get("ints"));
        assertNotSame(date, clone.get("date"));
        assertNotSame(sorted, clone.get("sorted"));
        assertSame("immutable values are shared", original.get("name"), clone.get("name"));

        // mutating the clone leaves the original untouched
        ((List<Object>) clone.get("tags")).add("c");
        ((Document) ((List<?>) clone.get("docs")).get(0)).put("n", 2);
        ((List<Object>) ((Map<?, ?>) clone.get("map")).get("k")).clear();
        ((byte[]) clone.get("bytes"))[0] = 9;
        ((int[]) clone.get("ints"))[0] = 9;
        ((Date) clone.get("date")).setTime(2000L);
        ((List<Object>) clone.get("fixed")).add("r");
        assertEquals(Arrays.asList("a", "b"), tags);
        assertEquals(1, embedded.get("n"));
        assertEquals(Collections.singletonList("v"), map.get("k"));
        assertEquals(1, bytes[0]);
        assertEquals(1, ints[0]);
        assertEquals(1000L, date.getTime());
        assertEquals(2, ((List<?>) original.get("fixed")).size());

        // container types and comparators survive the copy
        assertTrue(clone.get("tags") instanceof LinkedList);
        assertTrue(clone.get("map") instanceof HashMap);
        SortedSet<?> sortedCopy = (SortedSet<?>) clone.get("sorted");
        assertEquals("y", sortedCopy.first());
        assertNotNull(sortedCopy.comparator());
    }

    @Test
    public void testContainsKey() {
        assertFalse((new NitriteDocument()).containsKey("key"));
    }

    @Test
    public void testEquals() {
        assertFalse((new NitriteDocument()).equals("Other"));
    }

    @Test
    public void testEquals2() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        assertTrue(nitriteDocument.equals(new NitriteDocument()));
    }

    @Test
    public void testEquals3() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("Field", "Value");
        assertFalse(nitriteDocument.equals(new NitriteDocument()));
    }

    @Test
    public void testEquals4() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("Field", "Value");

        NitriteDocument nitriteDocument1 = new NitriteDocument();
        nitriteDocument1.put("Field", "Value");
        assertTrue(nitriteDocument.equals(nitriteDocument1));
    }

    @Test
    public void testEquals5() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("Field", "Value");

        NitriteDocument nitriteDocument1 = new NitriteDocument();
        nitriteDocument1.putIfAbsent("foo", "42");
        assertFalse(nitriteDocument.equals(nitriteDocument1));
    }

    @Test
    public void testEquals6() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("Field", null);

        NitriteDocument nitriteDocument1 = new NitriteDocument();
        nitriteDocument1.put("Field", "Value");
        assertFalse(nitriteDocument.equals(nitriteDocument1));
    }

    @Test
    public void testEquals7() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("Field", new NitriteDocument());

        NitriteDocument nitriteDocument1 = new NitriteDocument();
        nitriteDocument1.put("Field", "Value");
        assertFalse(nitriteDocument.equals(nitriteDocument1));
    }

    @Test
    public void testEquals8() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("Field", null);

        NitriteDocument nitriteDocument1 = new NitriteDocument();
        nitriteDocument1.putIfAbsent("foo", "42");
        assertFalse(nitriteDocument.equals(nitriteDocument1));
    }

    @Test
    public void testEquals9() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("Field", null);

        NitriteDocument nitriteDocument1 = new NitriteDocument();
        nitriteDocument1.put("Field", null);
        assertTrue(nitriteDocument.equals(nitriteDocument1));
    }
}

