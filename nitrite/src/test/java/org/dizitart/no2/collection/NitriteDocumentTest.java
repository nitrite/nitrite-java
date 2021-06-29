package org.dizitart.no2.collection;

import com.fasterxml.jackson.databind.introspect.AnnotatedMethodMap;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Set;

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
        nitriteDocument.putIfAbsent("_id", "42");
        assertEquals("42", nitriteDocument.getId().getIdValue());
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

