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
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.data.ChildClass;
import org.dizitart.no2.repository.data.Employee;
import org.junit.Test;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    public void testDeepEquals2() {
        CharArraySet makeStopSetResult = StopFilter.makeStopSet(new String[]{"foo", "foo", "foo"}, true);
        makeStopSetResult.add((Object) "foo");
        assertFalse(ObjectUtils.deepEquals(makeStopSetResult, new AnnotatedMethodMap()));
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
    public void testDeepEquals6() {
        CharArraySet makeStopSetResult = StopFilter.makeStopSet(new String[]{"foo", "foo", "foo"}, true);
        makeStopSetResult.add((Object) "foo");
        CharArraySet makeStopSetResult1 = StopFilter.makeStopSet(new String[]{"foo", "foo", "foo"}, true);
        makeStopSetResult1.add((Object) "foo");
        assertTrue(ObjectUtils.deepEquals(makeStopSetResult, makeStopSetResult1));
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
        EnclosingType type = newInstance(EnclosingType.class, true);
        System.out.println(type);
    }

    @Test
    public void testIsValueType() {
        assertFalse(ObjectUtils.isValueType(Object.class));
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
    }

    @Data
    private static class FieldType {
        private Employee employee;
        private LocalDateTime currentDate;
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
        @Index(value = "value")
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
