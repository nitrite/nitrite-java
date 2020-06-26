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

package org.dizitart.no2.test;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.mapper.JacksonMapperModule;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.test.data.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class ObjectRepositoryTest {
    private String dbPath = BaseObjectRepositoryTest.getRandomTempDbFile();
    private Nitrite db;

    @Before
    public void setUp() {
        db = NitriteBuilder.get()
            .loadModule(new JacksonMapperModule())
            .filePath(dbPath)
            .openOrCreate();
    }

    @After
    public void close() throws IOException {
        db.close();
        db = null;
        Files.delete(Paths.get(dbPath));
    }

    @Test
    public void testWithClassField() {
        ObjectRepository<WithClassField> repository = db.getRepository(WithClassField.class);

        WithClassField object = new WithClassField();
        object.setName("test");
        object.setClazz(String.class);

        repository.insert(object);
        WithClassField instance = repository.getById("test");
        assertEquals(instance.getName(), object.getName());
        assertEquals(instance.getClazz(), object.getClazz());
    }

    @Test
    public void testWithFinalField() {
        ObjectRepository<WithFinalField> repository = db.getRepository(WithFinalField.class);
        WithFinalField object = new WithFinalField();
        object.setName("test");

        repository.insert(object);
        for (WithFinalField instance : repository.find()) {
            assertEquals(object.getName(), instance.getName());
            assertEquals(object.getNumber(), instance.getNumber());
        }
    }

    @Test
    public void testWithOutGetterSetter() {
        ObjectRepository<WithOutGetterSetter> repository = db.getRepository(WithOutGetterSetter.class);
        WithOutGetterSetter object = new WithOutGetterSetter();

        repository.insert(object);
        for (WithOutGetterSetter instance : repository.find()) {
            assertEquals(object, instance);
        }
    }

    @Test
    public void testWithOutId() {
        ObjectRepository<WithOutId> repository = db.getRepository(WithOutId.class);
        WithOutId object = new WithOutId();
        object.setName("test");
        object.setNumber(2);

        repository.insert(object);
        for (WithOutId instance : repository.find()) {
            assertEquals(object.getName(), instance.getName());
            assertEquals(object.getNumber(), instance.getNumber());
        }
    }

    @Test
    public void testWithPublicField() {
        ObjectRepository<WithPublicField> repository = db.getRepository(WithPublicField.class);
        WithPublicField object = new WithPublicField();
        object.name = "test";
        object.number = 2;

        repository.insert(object);
        WithPublicField instance = repository.getById("test");
        assertEquals(object.name, instance.name);
        assertEquals(object.number, instance.number);
    }

    @Test
    public void testWithTransientField() {
        ObjectRepository<WithTransientField> repository = db.getRepository(WithTransientField.class);
        WithTransientField object = new WithTransientField();
        object.setNumber(2);
        object.setName("test");

        repository.insert(object);
        WithTransientField instance = repository.getById(2L);
        assertNotEquals(object.getName(), instance.getName());
        assertNull(instance.getName());
        assertEquals(object.getNumber(), instance.getNumber());
    }

    @Test
    public void testWriteThousandRecords() {
        int count = 5000;

        ObjectRepository<StressRecord> repository = db.getRepository(StressRecord.class);

        for (int i = 0; i < count; i++) {
            StressRecord record = new StressRecord();
            record.setFirstName(UUID.randomUUID().toString());
            record.setFailed(false);
            record.setLastName(UUID.randomUUID().toString());
            record.setProcessed(false);

            repository.insert(record);
        }

        Cursor<StressRecord> cursor
            = repository.find(where("failed").eq(false));
        for (StressRecord record : cursor) {
            record.setProcessed(true);
            repository.update(where("firstName").eq(record.getFirstName()), record);
        }
    }

    @Test
    public void testWithPackagePrivateClass() {
        ObjectRepository<InternalClass> repository = db.getRepository(InternalClass.class);
        InternalClass internalClass = new InternalClass();
        internalClass.setId(1);
        internalClass.setName("name");

        repository.insert(internalClass);
        InternalClass instance = repository.getById((long) 1);
        assertEquals(internalClass.getName(), instance.getName());
        assertEquals(internalClass.getId(), instance.getId());
    }

    @Test
    public void testWithPrivateConstructor() {
        ObjectRepository<WithPrivateConstructor> repository =
            db.getRepository(WithPrivateConstructor.class);

        WithPrivateConstructor object = WithPrivateConstructor.create("test", 2L);
        repository.insert(object);
        for (WithPrivateConstructor instance : repository.find()) {
            assertEquals(object, instance);
        }
    }

    @Test
    public void testWithDateAsId() {
        ObjectRepository<WithDateId> repository = db.getRepository(WithDateId.class);

        WithDateId object1 = new WithDateId();
        object1.setId(new Date(1482773634L));
        object1.setName("first date");
        repository.insert(object1);

        WithDateId object2 = new WithDateId();
        object2.setName("second date");
        object2.setId(new Date(1482773720L));
        repository.insert(object2);

        assertEquals(repository.find(where("id").eq(new Date(1482773634L)))
            .firstOrNull(), object1);
        assertEquals(repository.find(where("id").eq(new Date(1482773720L)))
            .firstOrNull(), object2);
    }

    @Test
    public void testWithIdInheritance() {
        ObjectRepository<ChildClass> repository = db.getRepository(ChildClass.class);
        assertTrue(repository.hasIndex("id"));
        assertTrue(repository.hasIndex("date"));
        assertTrue(repository.hasIndex("text"));

        ChildClass childClass = new ChildClass();
        childClass.setName("first");
        childClass.setDate(new Date(100000L));
        childClass.setId(1L);
        childClass.setText("I am first class");
        repository.insert(childClass);

        childClass = new ChildClass();
        childClass.setName("seconds");
        childClass.setDate(new Date(100001L));
        childClass.setId(2L);
        childClass.setText("I am second class");
        repository.insert(childClass);

        childClass = new ChildClass();
        childClass.setName("third");
        childClass.setDate(new Date(100002L));
        childClass.setId(3L);
        childClass.setText("I am third class");
        repository.insert(childClass);

        assertEquals(repository.find(where("text").text("class")).size(), 3);
        assertEquals(repository.find(where("text").text("second")).size(), 0); // filtered in stop words
        assertEquals(repository.find(where("date").eq(new Date(100000L))).size(), 1);
        assertEquals(repository.find(where("id").eq(1L)).size(), 1);
    }

    @Test
    public void testAttributes() {
        ObjectRepository<WithDateId> repository = db.getRepository(WithDateId.class);
        Attributes attributes = new Attributes(repository.getDocumentCollection().getName());
        repository.setAttributes(attributes);
        assertEquals(repository.getAttributes(), attributes);
    }
}
