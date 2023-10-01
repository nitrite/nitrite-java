package org.dizitart.no2.common.mapper;

import org.dizitart.no2.integration.NitriteStressTest;
import org.dizitart.no2.integration.NitriteTest;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleNitriteMapperTest {
    @Test
    public void testConvert() {
        SimpleNitriteMapper simpleNitriteMapper = new SimpleNitriteMapper(Integer.class);
        assertEquals(0, ((Integer) simpleNitriteMapper.<Object, Object>tryConvert(0, Object.class)).intValue());
    }

    @Test
    public void testConvert2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleNitriteMapper simpleNitriteMapper = new SimpleNitriteMapper(forNameResult, forNameResult1, Object.class);
        assertEquals("source", simpleNitriteMapper.<Object, Object>tryConvert("source", Object.class));
    }

    @Test
    public void testConvert3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleNitriteMapper simpleNitriteMapper = new SimpleNitriteMapper(forNameResult, forNameResult1, Object.class);
        assertEquals(0, ((Integer) simpleNitriteMapper.<Object, Object>tryConvert(0, Object.class)).intValue());
    }

    @Test
    public void testConvertFromDocument() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleNitriteMapper simpleNitriteMapper = new SimpleNitriteMapper(forNameResult, forNameResult1, Object.class);
        assertNull(simpleNitriteMapper.convertFromDocument(null, Object.class));
    }

    @Test
    public void testConvertFromDocument2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleNitriteMapper simpleNitriteMapper = new SimpleNitriteMapper(forNameResult, forNameResult1, Object.class);
        Class type = Object.class;
        assertThrows(ObjectMappingException.class,
            () -> simpleNitriteMapper.convertFromDocument(Document.createDocument(), type));
    }

    @Test
    public void testConvertFromDocument3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        assertNull(
            (new SimpleNitriteMapper(forNameResult, forNameResult1, Object.class)).convertFromDocument(null, null));
    }

    @Test
    public void testConvertToDocument() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        assertThrows(ObjectMappingException.class,
            () -> (new SimpleNitriteMapper(forNameResult, forNameResult1, Object.class)).<Object>convertToDocument("source"));
    }

    @Test
    public void testConvertToDocument2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleNitriteMapper simpleNitriteMapper = new SimpleNitriteMapper(forNameResult, forNameResult1, Object.class);
        simpleNitriteMapper.registerEntityConverter(new NitriteTest.CompatChild.CompatChildConverter());
        assertEquals(2, simpleNitriteMapper.<Object>convertToDocument(new NitriteTest.CompatChild()).size());
    }

    @Test
    public void testConvertToDocument3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleNitriteMapper simpleNitriteMapper = new SimpleNitriteMapper(forNameResult, forNameResult1, Object.class);
        simpleNitriteMapper.registerEntityConverter(new NitriteStressTest.TestDto.Converter());
        assertEquals(7, simpleNitriteMapper.<Object>convertToDocument(new NitriteStressTest.TestDto()).size());
    }

    @Test
    public void testConvertToDocument4() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleNitriteMapper simpleNitriteMapper = new SimpleNitriteMapper(forNameResult, forNameResult1, Object.class);
        simpleNitriteMapper.registerEntityConverter(new Company.CompanyConverter());
        assertEquals(3, simpleNitriteMapper.<Object>convertToDocument(new Company()).size());
    }

}

