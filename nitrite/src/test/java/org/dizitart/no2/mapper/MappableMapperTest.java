package org.dizitart.no2.mapper;

import org.dizitart.no2.NitriteStressTest;
import org.dizitart.no2.NitriteTest;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.junit.Test;

import static org.junit.Assert.*;

public class MappableMapperTest {
    @Test
    public void testConvert() {
        MappableMapper mappableMapper = new MappableMapper(null);
        assertEquals(0, ((Integer) mappableMapper.<Object, Object>convert(0, Object.class)).intValue());
    }

    @Test
    public void testConvert2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        MappableMapper mappableMapper = new MappableMapper(forNameResult, forNameResult1, Object.class);
        assertEquals("source", mappableMapper.<Object, Object>convert("source", Object.class));
    }

    @Test
    public void testConvert3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        MappableMapper mappableMapper = new MappableMapper(forNameResult, forNameResult1, Object.class);
        assertEquals(0, ((Integer) mappableMapper.<Object, Object>convert(0, Object.class)).intValue());
    }

    @Test
    public void testConvertFromDocument() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        MappableMapper mappableMapper = new MappableMapper(forNameResult, forNameResult1, Object.class);
        assertNull(mappableMapper.<Object>convertFromDocument(null, Object.class));
    }

    @Test
    public void testConvertFromDocument2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        MappableMapper mappableMapper = new MappableMapper(forNameResult, forNameResult1, Object.class);
        Class type = Object.class;
        assertThrows(ObjectMappingException.class,
            () -> mappableMapper.convertFromDocument(Document.createDocument(), type));
    }

    @Test
    public void testConvertFromDocument3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        assertNull(
            (new MappableMapper(forNameResult, forNameResult1, Object.class)).convertFromDocument(null, null));
    }

    @Test
    public void testConvertToDocument() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        assertThrows(ObjectMappingException.class,
            () -> (new MappableMapper(forNameResult, forNameResult1, Object.class)).<Object>convertToDocument("source"));
    }

    @Test
    public void testConvertToDocument2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        MappableMapper mappableMapper = new MappableMapper(forNameResult, forNameResult1, Object.class);
        assertEquals(2, mappableMapper.<Object>convertToDocument(new NitriteTest.CompatChild()).size());
    }

    @Test
    public void testConvertToDocument3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        MappableMapper mappableMapper = new MappableMapper(forNameResult, forNameResult1, Object.class);
        assertEquals(7, mappableMapper.<Object>convertToDocument(new NitriteStressTest.TestDto()).size());
    }

    @Test
    public void testConvertToDocument4() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        MappableMapper mappableMapper = new MappableMapper(forNameResult, forNameResult1, Object.class);
        assertEquals(3, mappableMapper.<Object>convertToDocument(new Company()).size());
    }

    @Test
    public void testIsValueType() {
        MappableMapper mappableMapper = new MappableMapper();
        assertFalse(mappableMapper.isValueType(Object.class));
    }

    @Test
    public void testIsValueType2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        MappableMapper mappableMapper = new MappableMapper(forNameResult, forNameResult1, Object.class);
        assertTrue(mappableMapper.isValueType(Object.class));
    }

    @Test
    public void testIsValue() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        assertTrue((new MappableMapper(forNameResult, forNameResult1, Object.class)).isValue("object"));
    }
}

