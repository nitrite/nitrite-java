package org.dizitart.no2.common.mapper;

import org.dizitart.no2.integration.NitriteStressTest;
import org.dizitart.no2.integration.NitriteTest;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleDocumentMapperTest {
    @Test
    public void testConvert() {
        SimpleDocumentMapper simpleDocumentMapper = new SimpleDocumentMapper(Integer.class);
        assertEquals(0, ((Integer) simpleDocumentMapper.<Object, Object>convert(0, Object.class)).intValue());
    }

    @Test
    public void testConvert2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleDocumentMapper simpleDocumentMapper = new SimpleDocumentMapper(forNameResult, forNameResult1, Object.class);
        assertEquals("source", simpleDocumentMapper.<Object, Object>convert("source", Object.class));
    }

    @Test
    public void testConvert3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleDocumentMapper simpleDocumentMapper = new SimpleDocumentMapper(forNameResult, forNameResult1, Object.class);
        assertEquals(0, ((Integer) simpleDocumentMapper.<Object, Object>convert(0, Object.class)).intValue());
    }

    @Test
    public void testConvertFromDocument() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleDocumentMapper simpleDocumentMapper = new SimpleDocumentMapper(forNameResult, forNameResult1, Object.class);
        assertNull(simpleDocumentMapper.convertFromDocument(null, Object.class));
    }

    @Test
    public void testConvertFromDocument2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleDocumentMapper simpleDocumentMapper = new SimpleDocumentMapper(forNameResult, forNameResult1, Object.class);
        Class type = Object.class;
        assertThrows(ObjectMappingException.class,
            () -> simpleDocumentMapper.convertFromDocument(Document.createDocument(), type));
    }

    @Test
    public void testConvertFromDocument3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        assertNull(
            (new SimpleDocumentMapper(forNameResult, forNameResult1, Object.class)).convertFromDocument(null, null));
    }

    @Test
    public void testConvertToDocument() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        assertThrows(ObjectMappingException.class,
            () -> (new SimpleDocumentMapper(forNameResult, forNameResult1, Object.class)).<Object>convertToDocument("source"));
    }

    @Test
    public void testConvertToDocument2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleDocumentMapper simpleDocumentMapper = new SimpleDocumentMapper(forNameResult, forNameResult1, Object.class);
        simpleDocumentMapper.registerEntityConverter(new NitriteTest.CompatChild.CompatChildConverter());
        assertEquals(2, simpleDocumentMapper.<Object>convertToDocument(new NitriteTest.CompatChild()).size());
    }

    @Test
    public void testConvertToDocument3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleDocumentMapper simpleDocumentMapper = new SimpleDocumentMapper(forNameResult, forNameResult1, Object.class);
        simpleDocumentMapper.registerEntityConverter(new NitriteStressTest.TestDto.Converter());
        assertEquals(7, simpleDocumentMapper.<Object>convertToDocument(new NitriteStressTest.TestDto()).size());
    }

    @Test
    public void testConvertToDocument4() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        SimpleDocumentMapper simpleDocumentMapper = new SimpleDocumentMapper(forNameResult, forNameResult1, Object.class);
        simpleDocumentMapper.registerEntityConverter(new Company.CompanyConverter());
        assertEquals(3, simpleDocumentMapper.<Object>convertToDocument(new Company()).size());
    }

}

