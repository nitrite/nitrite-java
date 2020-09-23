package org.dizitart.no2.collection;

import com.fasterxml.jackson.databind.introspect.AnnotatedMethodMap;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

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
        assertNull((new NitriteDocument()).<Object>get(null, type));
    }

    @Test
    public void testGet2() {
        Class type = Object.class;
        assertNull((new NitriteDocument()).<Object>get("key", type));
    }

    @Test
    public void testGet3() {
        assertNull((new NitriteDocument()).get("key"));
    }

    @Test
    public void testGetFields() {
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
        assertEquals(0, (new NitriteDocument()).getFields().size());
    }

    @Test
    public void testHasId() {
        assertFalse((new NitriteDocument()).hasId());
    }

    @Test
    public void testClone() {
        assertEquals(0, (new NitriteDocument()).clone().size());
    }

    @Test
    public void testContainsKey() {
        assertFalse((new NitriteDocument()).containsKey("key"));
    }

    @Test
    public void testEquals() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        nitriteDocument.put("foo", "foo");
        assertFalse(nitriteDocument.equals(new NitriteDocument()));
    }

    @Test
    public void testEquals2() {
        assertFalse((new NitriteDocument()).equals("other"));
    }

    @Test
    public void testEquals3() {
        NitriteDocument nitriteDocument = new NitriteDocument();
        assertTrue(nitriteDocument.equals(new NitriteDocument()));
    }
}

