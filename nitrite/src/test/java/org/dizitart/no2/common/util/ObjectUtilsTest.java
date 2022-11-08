/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.common.util;

import com.fasterxml.jackson.databind.introspect.AnnotatedMethodMap;
import junit.framework.JUnit4TestAdapterCache;
import lombok.Data;
import org.apache.commons.lang3.mutable.MutableByte;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.SimpleDocumentMapper;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.integration.NitriteTest;
import org.dizitart.no2.integration.repository.data.ChildClass;
import org.dizitart.no2.integration.repository.data.Company;
import org.dizitart.no2.integration.repository.data.Employee;
import org.dizitart.no2.integration.repository.data.Note;
import org.dizitart.no2.integration.repository.decorator.Manufacturer;
import org.dizitart.no2.integration.repository.decorator.ManufacturerDecorator;
import org.dizitart.no2.integration.repository.decorator.ProductDecorator;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Index;
import org.junit.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

import static org.dizitart.no2.common.util.ObjectUtils.newInstance;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class ObjectUtilsTest implements Serializable {

    @Test
    public void testGetEntityName() {
        assertEquals("java.lang.Object", ObjectUtils.getEntityName(Object.class));
    }

    @Test
    public void testFindRepositoryName() {
        assertEquals("entityName", ObjectUtils.findRepositoryName("entityName", ""));
        assertEquals("entityName+key", ObjectUtils.findRepositoryName("entityName", "key"));
        assertEquals("java.lang.Object+key", ObjectUtils.findRepositoryName(Object.class, "key"));
        assertEquals("java.lang.Object", ObjectUtils.findRepositoryName(Object.class, ""));
    }

    @Test
    public void testFindRepositoryNameByDecorator() {
        assertEquals("product", ObjectUtils.findRepositoryNameByDecorator(new ProductDecorator(), ""));
        assertEquals("product+key", ObjectUtils.findRepositoryNameByDecorator(new ProductDecorator(), "key"));
        assertEquals(Manufacturer.class.getName() + "+key",
            ObjectUtils.findRepositoryNameByDecorator(new ManufacturerDecorator(), "key"));
        assertEquals(Manufacturer.class.getName(),
            ObjectUtils.findRepositoryNameByDecorator(new ManufacturerDecorator(), ""));
    }

    @Test
    public void testDeepEquals() {
        assertFalse(ObjectUtils.deepEquals("o1", "o2"));
        assertFalse(ObjectUtils.deepEquals(null, "o2"));
        assertFalse(ObjectUtils.deepEquals(new AnnotatedMethodMap(), "o2"));
        assertTrue(ObjectUtils.deepEquals(null, null));
        assertFalse(ObjectUtils.deepEquals(new MutableByte(), "o2"));
        assertFalse(ObjectUtils.deepEquals(new JUnit4TestAdapterCache(), "o2"));
        assertFalse(ObjectUtils.deepEquals("o1", null));
    }

    @Test
    public void testDeepEquals3() {
        MutableByte o1 = new MutableByte();
        assertFalse(ObjectUtils.deepEquals(o1, new MutableDouble()));
    }

    @Test
    public void testDeepEquals4() {
        JUnit4TestAdapterCache o1 = new JUnit4TestAdapterCache();
        assertTrue(ObjectUtils.deepEquals(o1, new JUnit4TestAdapterCache()));
    }

    @Test
    public void testDeepEquals5() {
        MutableByte o1 = new MutableByte();
        assertTrue(ObjectUtils.deepEquals(o1, new MutableByte()));
    }

    @Test
    public void testDeepEquals7() {
        AnnotatedMethodMap o1 = new AnnotatedMethodMap();
        assertTrue(ObjectUtils.deepEquals(o1, new AnnotatedMethodMap()));
    }

    @Test
    public void testDeepEquals8() {
        MutableByte o2 = new MutableByte((byte) 65);
        assertFalse(ObjectUtils.deepEquals(new MutableByte(), o2));
    }

    @Test
    public void testNewInstance() {
        SimpleDocumentMapper mapper = new SimpleDocumentMapper();
        mapper.registerEntityConverter(new EnclosingType.Converter());
        mapper.registerEntityConverter(new ChildClass.Converter());
        mapper.registerEntityConverter(new FieldType.Converter());
        mapper.registerEntityConverter(new Employee.EmployeeConverter());
        mapper.registerEntityConverter(new Company.CompanyConverter());
        mapper.registerEntityConverter(new Note.NoteConverter());

        EnclosingType type = newInstance(EnclosingType.class, true, mapper);
        assertNotNull(type);

        assertThrows(ObjectMappingException.class, () -> newInstance(Object.class, false, mapper));
        assertThrows(ObjectMappingException.class, () -> newInstance(NitriteTest.Receipt.class, false, mapper));

        assertNull(newInstance(byte[].class, false, mapper));
        assertNull(newInstance(Number.class, false, mapper));
        assertNull(newInstance(Byte.class, false, mapper));
        assertNull(newInstance(Short.class, false, mapper));
        assertNull(newInstance(Integer.class, false, mapper));
        assertNull(newInstance(Long.class, false, mapper));
        assertNull(newInstance(Float.class, false, mapper));
        assertNull(newInstance(Double.class, false, mapper));
        assertNull(newInstance(BigDecimal.class, false, mapper));
        assertNull(newInstance(BigInteger.class, false, mapper));
        assertNull(newInstance(Boolean.class, false, mapper));
        assertNull(newInstance(Character.class, false, mapper));
        assertNull(newInstance(String.class, false, mapper));
        assertNull(newInstance(Date.class, false, mapper));
        assertNull(newInstance(URL.class, false, mapper));
        assertNull(newInstance(URI.class, false, mapper));
        assertNull(newInstance(Currency.class, false, mapper));
        assertNull(newInstance(Calendar.class, false, mapper));
        assertNull(newInstance(StringBuffer.class, false, mapper));
        assertNull(newInstance(StringBuilder.class, false, mapper));
        assertNull(newInstance(Locale.class, false, mapper));
        assertNull(newInstance(Void.class, false, mapper));
        assertNull(newInstance(UUID.class, false, mapper));
        assertNull(newInstance(Pattern.class, false, mapper));

        assertNull(newInstance(GregorianCalendar.class, false, mapper));
    }

    @Test
    public void testIsCompatibleTypes() {
        Class<?> type1 = Object.class;
        assertTrue(ObjectUtils.isCompatibleTypes(type1, Object.class));
    }

    @Test
    public void testDeepCopy() {
        assertNull(ObjectUtils.deepCopy(null));
        assertEquals(NitriteId.createId("42"), ObjectUtils.deepCopy(NitriteId.createId("42")));
        assertNotEquals(NitriteId.createId("41"), ObjectUtils.deepCopy(NitriteId.createId("42")));
        assertEquals(Document.createDocument("foo", "foo"),
            ObjectUtils.deepCopy(Document.createDocument("foo", "foo")));

        // equals() not implemented so reference check should not be equal
        assertNotEquals(this, ObjectUtils.deepCopy(this));
    }

    @Test(expected = ValidationException.class)
    public void testInvalidEntity1() {
        ObjectUtils.getEntityName(InvalidEntity1.class);
    }

    @Test(expected = ValidationException.class)
    public void testInvalidEntity2() {
        ObjectUtils.getEntityName(InvalidEntity2.class);
    }

    @Test
    public void testValidEntity() {
        assertEquals("org.dizitart.no2.common.util.ObjectUtilsTest$ValidEntity3",
            ObjectUtils.getEntityName(ValidEntity3.class));
        assertEquals("org.dizitart.no2.common.util.ObjectUtilsTest$ValidEntity4",
            ObjectUtils.getEntityName(ValidEntity4.class));
        assertEquals("org.dizitart.no2.common.util.ObjectUtilsTest$ValidEntity5",
            ObjectUtils.getEntityName(ValidEntity5.class));
        assertEquals("a-b", ObjectUtils.getEntityName(ValidEntity6.class));
    }

    @Data
    private static class EnclosingType {
        private ChildClass childClass;
        private FieldType fieldType;

        public static class Converter implements EntityConverter<EnclosingType> {

            @Override
            public Class<EnclosingType> getEntityType() {
                return EnclosingType.class;
            }

            @Override
            public Document toDocument(EnclosingType entity, NitriteMapper nitriteMapper) {
                return Document.createDocument()
                    .put("childClass", nitriteMapper.convert(entity.childClass, Document.class))
                    .put("fieldType", nitriteMapper.convert(entity.fieldType, Document.class));
            }

            @Override
            public EnclosingType fromDocument(Document document, NitriteMapper nitriteMapper) {
                EnclosingType entity = new EnclosingType();
                entity.childClass = nitriteMapper.convert(document.get("childClass", Document.class),
                    ChildClass.class);
                entity.fieldType = nitriteMapper.convert(document.get("fieldType", Document.class),
                    FieldType.class);
                return entity;
            }
        }
    }

    @Data
    private static class FieldType {
        private Employee employee;
        private LocalDateTime currentDate;

        public static class Converter implements EntityConverter<FieldType> {

            @Override
            public Class<FieldType> getEntityType() {
                return FieldType.class;
            }

            @Override
            public Document toDocument(FieldType entity, NitriteMapper nitriteMapper) {
                return Document.createDocument()
                    .put("currentDate", entity.currentDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .put("employee", nitriteMapper.convert(entity.employee, Document.class));
            }

            @Override
            public FieldType fromDocument(Document document, NitriteMapper nitriteMapper) {
                FieldType entity = new FieldType();
                entity.employee = nitriteMapper.convert(document.get("employee", Document.class), Employee.class);
                if (document.get("currentDate", Long.class) != null) {
                    entity.currentDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(document.get("currentDate", Long.class)),
                        ZoneId.systemDefault());
                }
                return entity;
            }
        }
    }

    @Data
    @Entity(value = "a+b")
    private static class InvalidEntity1 {
        private String value;
    }

    @Data
    @Entity(value = "+")
    private static class InvalidEntity2 {
        private String value;
    }

    @Data
    @Entity(value = "")
    private static class ValidEntity3 {
        private String value;
    }

    @Data
    @Entity
    private static class ValidEntity4 {
        private String value;
    }

    @Data
    @Entity(indices = {
        @Index(fields = "value")
    })
    private static class ValidEntity5 {
        private String value;
    }

    @Data
    @Entity(value = "a-b")
    private static class ValidEntity6 {
        private String value;
    }
}
