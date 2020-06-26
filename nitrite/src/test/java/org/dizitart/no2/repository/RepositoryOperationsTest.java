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

package org.dizitart.no2.repository;

import lombok.Getter;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.util.ObjectUtilsTest;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.InheritIndices;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.repository.data.Employee;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Set;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryOperationsTest {
    private RepositoryOperations operations;
    private Nitrite db;

    @Before
    public void setUp() {
        db = NitriteBuilder.get().openOrCreate();
    }

    @Test
    public void testIndexes() {
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<TestObjectWithIndex> repository = db.getRepository(TestObjectWithIndex.class);
        operations = new RepositoryOperations(TestObjectWithIndex.class, nitriteMapper, repository.getDocumentCollection());
        Set<Index> indexes = operations.extractIndices(TestObjectWithIndex.class);
        assertEquals(indexes.size(), 2);
    }

    @Test(expected = IndexingException.class)
    public void testInvalidIndexNonComparable() {
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<ObjectWithNonComparableIndex> repository = db.getRepository(ObjectWithNonComparableIndex.class);
        operations = new RepositoryOperations(TestObjectWithIndex.class, nitriteMapper, repository.getDocumentCollection());
        Set<Index> indexes = operations.extractIndices(ObjectWithNonComparableIndex.class);
        assertEquals(indexes.size(), 2);
    }

    @Test(expected = IndexingException.class)
    public void testInvalidIndexComparableAndIterable() {
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<ObjectWithIterableIndex> repository = db.getRepository(ObjectWithIterableIndex.class);
        operations = new RepositoryOperations(ObjectWithIterableIndex.class, nitriteMapper, repository.getDocumentCollection());
        operations.extractIndices(ObjectWithIterableIndex.class);
    }

    @Test(expected = ValidationException.class)
    public void testGetFieldsUpToNullStartClass() {
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<Employee> repository = db.getRepository(Employee.class);
        operations = new RepositoryOperations(Employee.class, nitriteMapper, repository.getDocumentCollection());
        assertEquals(operations.getFieldsUpto(null, null).size(), 0);
    }

    @Test(expected = ValidationException.class)
    public void testGetFieldNoSuchField() {
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<ClassWithAnnotatedFields> repository = db.getRepository(ClassWithAnnotatedFields.class);
        operations = new RepositoryOperations(ClassWithAnnotatedFields.class, nitriteMapper, repository.getDocumentCollection());
        operations.getField(getClass(), "test");
    }

    @Test
    public void testGetFieldsUpTo() {
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<Employee> repository = db.getRepository(Employee.class);
        operations = new RepositoryOperations(TestObjectWithIndex.class, nitriteMapper, repository.getDocumentCollection());

        assertEquals(operations.getFieldsUpto(A.class, B.class).size(), 3);
        assertEquals(operations.getFieldsUpto(A.class, Object.class).size(), 5);
        assertEquals(operations.getFieldsUpto(A.class, null).size(), 5);

        assertEquals(operations.getFieldsUpto(ClassWithAnnotatedFields.class,
            Object.class).size(), 5);
        assertEquals(operations.getFieldsUpto(ClassWithAnnotatedFields.class,
            null).size(), 5);
        assertEquals(operations.getFieldsUpto(ClassWithAnnotatedFields.class,
            ClassWithNoAnnotatedFields.class).size(), 3);
    }

    @Test(expected = NotIdentifiableException.class)
    public void testCreateUniqueFilterInvalidId() {
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<B> repository = db.getRepository(B.class);
        operations = new RepositoryOperations(B.class, nitriteMapper, repository.getDocumentCollection());

        B b = new B();
        operations.createUniqueFilter(b);
    }

    @Test(expected = NotIdentifiableException.class)
    public void testGetIdFieldMultipleId() {
        class Test implements Mappable {
            @Id
            private String id1;

            @Id
            private Long id2;

            @Override
            public Document write(NitriteMapper mapper) {
                return createDocument()
                    .put("id1", id1)
                    .put("id2", id2);
            }

            @Override
            public void read(NitriteMapper mapper, Document document) {
                id1 = document.get("id1", String.class);
                id2 = document.get("id2", Long.class);
            }
        }
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<Test> repository = db.getRepository(Test.class);
        operations = new RepositoryOperations(Test.class, nitriteMapper, repository.getDocumentCollection());

        operations.getIdField(Test.class);
    }

    @Test(expected = ValidationException.class)
    public void testInvalidEmbeddedKey() {
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<ClassWithAnnotatedFields> repository = db.getRepository(ClassWithAnnotatedFields.class);
        operations = new RepositoryOperations(ClassWithAnnotatedFields.class, nitriteMapper, repository.getDocumentCollection());
        operations.getField(ClassWithAnnotatedFields.class, "..");
    }

    @Test(expected = ValidationException.class)
    public void testInvalidGetField() {
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<ClassWithAnnotatedFields> repository = db.getRepository(ClassWithAnnotatedFields.class);
        operations = new RepositoryOperations(ClassWithAnnotatedFields.class, nitriteMapper, repository.getDocumentCollection());
        operations.getField(ClassWithAnnotatedFields.class, "fake.fake");
    }

    @Test(expected = ValidationException.class)
    public void testExtractInvalidIndex() {
        @Index(value = "fake")
        class Test implements Mappable {
            private String test;

            @Override
            public Document write(NitriteMapper mapper) {
                return createDocument().put("test", test);
            }

            @Override
            public void read(NitriteMapper mapper, Document document) {
                test = document.get("test", String.class);
            }
        }

        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<Test> repository = db.getRepository(Test.class);
        operations = new RepositoryOperations(Test.class, nitriteMapper, repository.getDocumentCollection());

        operations.extractIndices(Test.class);
    }

    @Test
    public void testFindAnnotationFromInterface() {
        NitriteMapper nitriteMapper = db.getConfig().nitriteMapper();
        ObjectRepository<TestInterface> repository = db.getRepository(TestInterface.class);
        operations = new RepositoryOperations(TestInterface.class, nitriteMapper, repository.getDocumentCollection());

        Set<Index> indices = operations.extractIndices(TestInterface.class);
        assertFalse(indices.isEmpty());
    }

    @Index(value = "value")
    private interface Interface {
        String getValue();
    }

    private static class ClassWithAnnotatedFields extends ClassWithNoAnnotatedFields {
        @Deprecated
        Long longValue;
        private String stringValue;
        @Deprecated
        private String anotherValue;

        @Deprecated
        public ClassWithAnnotatedFields() {
        }

        @Override
        public Document write(NitriteMapper mapper) {
            return super.write(mapper)
                .put("stringValue", stringValue)
                .put("anotherValue", anotherValue)
                .put("longValue", longValue);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            super.read(mapper, document);
            stringValue = document.get("stringValue", String.class);
            anotherValue = document.get("anotherValue", String.class);
            longValue = document.get("longValue", Long.class);
        }
    }

    private static class ClassWithNoAnnotatedFields implements Mappable {
        private String stringValue;
        private Integer integer;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument().put("stringValue", stringValue)
                .put("integer", integer);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            stringValue = document.get("stringValue", String.class);
            integer = document.get("integer", Integer.class);
        }
    }

    private static class A extends B {
        private String a;
        private Long b;
        private Integer c;
    }

    private static class B implements Mappable {
        String a;
        private Short d;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument()
                .put("a", a)
                .put("d", d);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            a = document.get("a", String.class);
            d = document.get("d", Short.class);
        }
    }

    @Index(value = "testClass")
    private static class ObjectWithNonComparableIndex implements Mappable {
        private ObjectUtilsTest testClass;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument("testClass", testClass);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            testClass = document.get("testClass", ObjectUtilsTest.class);
        }
    }

    @Index(value = "testClass")
    private static class ObjectWithIterableIndex implements Mappable {
        private TestClass testClass;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument()
                .put("testClass", testClass.write(mapper));
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            TestClass tc = new TestClass();
            tc.read(mapper, document.get("testClass", Document.class));
            testClass = tc;
        }
    }

    private static class TestClass implements Comparable<TestClass>, Iterable<Long>, Mappable {
        @Override
        public int compareTo(TestClass o) {
            return 0;
        }

        @Override
        public Iterator<Long> iterator() {
            return null;
        }

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument();
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {

        }
    }

    @InheritIndices
    private static class TestInterface implements Interface, Mappable {
        @Getter
        private String value;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument().put("value", value);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            value = document.get("value", String.class);
        }
    }

    @Index(value = "longValue")
    @Index(value = "decimal")
    private class TestObjectWithIndex implements Mappable {
        private long longValue;

        private TestObject testObject;

        private BigDecimal decimal;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument()
                .put("longValue", longValue)
                .put("testObject", testObject.write(mapper))
                .put("decimal", decimal.toPlainString());
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            longValue = document.get("longValue", Long.class);
            testObject = new TestObject();
            testObject.read(mapper, document.get("testObject", Document.class));
            String decimalString = document.get("decimal", String.class);
            decimal = new BigDecimal(decimalString);
        }
    }

    @Index(value = "longValue")
    private class TestObject implements Mappable {
        private String stringValue;

        private Long longValue;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument()
                .put("stringValue", stringValue)
                .put("longValue", longValue);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            stringValue = document.get("stringValue", String.class);
            longValue = document.get("longValue", Long.class);
        }
    }
}
