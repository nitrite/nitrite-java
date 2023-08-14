package org.dizitart.no2.common.mapper;

import org.dizitart.no2.integration.NitriteStressTest;
import org.dizitart.no2.integration.NitriteTest;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.junit.Test;

import static org.junit.Assert.*;

public class EntityConverterMapperTest {
    @Test
    public void testConvert() {
        EntityConverterMapper entityConverterMapper = new EntityConverterMapper(Integer.class);
        assertEquals(0, ((Integer) entityConverterMapper.<Object, Object>tryConvert(0, Object.class)).intValue());
    }

    @Test
    public void testConvert2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        EntityConverterMapper entityConverterMapper = new EntityConverterMapper(forNameResult, forNameResult1, Object.class);
        assertEquals("source", entityConverterMapper.<Object, Object>tryConvert("source", Object.class));
    }

    @Test
    public void testConvert3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        EntityConverterMapper entityConverterMapper = new EntityConverterMapper(forNameResult, forNameResult1, Object.class);
        assertEquals(0, ((Integer) entityConverterMapper.<Object, Object>tryConvert(0, Object.class)).intValue());
    }

    @Test
    public void testConvertFromDocument() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        EntityConverterMapper entityConverterMapper = new EntityConverterMapper(forNameResult, forNameResult1, Object.class);
        assertNull(entityConverterMapper.convertFromDocument(null, Object.class));
    }

    @Test
    public void testConvertFromDocument2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        EntityConverterMapper entityConverterMapper = new EntityConverterMapper(forNameResult, forNameResult1, Object.class);
        Class type = Object.class;
        assertThrows(ObjectMappingException.class,
            () -> entityConverterMapper.convertFromDocument(Document.createDocument(), type));
    }

    @Test
    public void testConvertFromDocument3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        assertNull(
            (new EntityConverterMapper(forNameResult, forNameResult1, Object.class)).convertFromDocument(null, null));
    }

    @Test
    public void testConvertToDocument() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        assertThrows(ObjectMappingException.class,
            () -> (new EntityConverterMapper(forNameResult, forNameResult1, Object.class)).<Object>convertToDocument("source"));
    }

    @Test
    public void testConvertToDocument2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        EntityConverterMapper entityConverterMapper = new EntityConverterMapper(forNameResult, forNameResult1, Object.class);
        entityConverterMapper.registerEntityConverter(new NitriteTest.CompatChild.CompatChildConverter());
        assertEquals(2, entityConverterMapper.<Object>convertToDocument(new NitriteTest.CompatChild()).size());
    }

    @Test
    public void testConvertToDocument3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        EntityConverterMapper entityConverterMapper = new EntityConverterMapper(forNameResult, forNameResult1, Object.class);
        entityConverterMapper.registerEntityConverter(new NitriteStressTest.TestDto.Converter());
        assertEquals(7, entityConverterMapper.<Object>convertToDocument(new NitriteStressTest.TestDto()).size());
    }

    @Test
    public void testConvertToDocument4() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        EntityConverterMapper entityConverterMapper = new EntityConverterMapper(forNameResult, forNameResult1, Object.class);
        entityConverterMapper.registerEntityConverter(new Company.CompanyConverter());
        assertEquals(3, entityConverterMapper.<Object>convertToDocument(new Company()).size());
    }

}

