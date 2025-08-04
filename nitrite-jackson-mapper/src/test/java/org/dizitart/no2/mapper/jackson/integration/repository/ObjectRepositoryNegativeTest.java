/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.mapper.jackson.integration.repository;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.*;
import org.dizitart.no2.mapper.jackson.JacksonMapperModule;
import org.dizitart.no2.mapper.jackson.integration.repository.data.*;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class ObjectRepositoryNegativeTest {
    private final String dbPath = BaseObjectRepositoryTest.getRandomTempDbFile();
    private Nitrite db;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() {
        db = TestUtil.createDb(dbPath, new JacksonMapperModule());
    }

    @After
    public void close() {
        db.close();
        db = null;
        TestUtil.deleteDb(dbPath);
    }

    @Test(expected = ObjectMappingException.class)
    public void testWithCircularReference() {
        ObjectRepository<WithCircularReference> repository = db.getRepository(WithCircularReference.class);

        WithCircularReference parent = new WithCircularReference();
        parent.setName("parent");
        WithCircularReference object = new WithCircularReference();
        object.setName("test");
        object.setParent(parent);
        // circular reference
        parent.setParent(object);

        repository.insert(object);
        WithCircularReference instance = repository.getById("parent");
        assertEquals(instance.getName(), object.getName());
        assertEquals(instance.getParent().getName(), object.getParent().getName());
    }

    @Test(expected = ValidationException.class)
    public void testWithCustomConstructor() {
        ObjectRepository<WithCustomConstructor> repository = db.getRepository(WithCustomConstructor.class);

        WithCustomConstructor object = new WithCustomConstructor("test", 2L);

        repository.insert(object);
        WithCustomConstructor instance = repository.getById("test");
        assertEquals(object.getName(), instance.getName());
        assertEquals(object.getNumber(), instance.getNumber());
    }

    @Test(expected = InvalidIdException.class)
    public void testWithEmptyStringId() {
        ObjectRepository<WithEmptyStringId> repository = db.getRepository(WithEmptyStringId.class);
        WithEmptyStringId object = new WithEmptyStringId();
        object.setName(""); // empty id value

        WriteResult result = repository.insert(object);
        for (NitriteId id : result) {
            WithEmptyStringId instance = repository.getById(id);
            assertEquals(instance, object);
        }
    }

    @Test(expected = InvalidIdException.class)
    public void testWithNullId() {
        ObjectRepository<WithNullId> repository = db.getRepository(WithNullId.class);
        WithNullId object = new WithNullId();

        WriteResult result = repository.insert(object);
        for (NitriteId id : result) {
            WithNullId instance = repository.getById(id);
            assertEquals(instance, object);
        }
    }

    @Test(expected = ValidationException.class)
    public void testWithValueTypeRepository() {
        ObjectRepository<String> repository = db.getRepository(String.class);
        repository.insert("test");
    }

    @Test(expected = InvalidOperationException.class)
    public void testFindResultRemove() {
        ObjectRepository<Employee> repository = db.getRepository(Employee.class);
        repository.insert(DataGenerator.generateEmployee());
        RecordStream<Employee> result = repository.find();
        result.iterator().remove();
    }

    @Test(expected = IndexingException.class)
    public void testWithObjectId() {
        ObjectRepository<WithObjectId> repository = db.getRepository(WithObjectId.class);
        WithOutId id = new WithOutId();
        id.setName("test");
        id.setNumber(1);

        WithObjectId object = new WithObjectId();
        object.setWithOutId(id);
        repository.insert(object);
    }

    @Test(expected = NotIdentifiableException.class)
    public void testUpdateNoId() {
        ObjectRepository<WithOutId> repository = db.getRepository(WithOutId.class);
        WithOutId object = new WithOutId();
        object.setName("name");
        object.setNumber(1L);
        repository.update(object);
    }

    @Test(expected = NotIdentifiableException.class)
    public void testRemoveNoId() {
        ObjectRepository<WithOutId> repository = db.getRepository(WithOutId.class);
        WithOutId object = new WithOutId();
        object.setName("name");
        object.setNumber(1L);
        repository.remove(object);
    }

    @Test(expected = ValidationException.class)
    public void testProjectionFailedInstantiate() {
        ObjectRepository<WithOutId> repository = db.getRepository(WithOutId.class);
        WithOutId object = new WithOutId();
        object.setName("name");
        object.setNumber(1L);
        repository.insert(object);

        RecordStream<NitriteId> project = repository.find().project(NitriteId.class);
        assertNull(project.toList());
    }

    @Test(expected = ValidationException.class)
    public void testNullInsert() {
        ObjectRepository<WithOutId> repository = db.getRepository(WithOutId.class);
        repository.insert(null);
    }

    @Test(expected = InvalidIdException.class)
    public void testGetByNullId() {
        ObjectRepository<WithPublicField> repository = db.getRepository(WithPublicField.class);
        WithPublicField object = new WithPublicField();
        object.name = "test";
        object.number = 2;

        repository.insert(object);
        WithPublicField instance = repository.getById(null);
        assertEquals(object.name, instance.name);
        assertEquals(object.number, instance.number);
    }

    @Test
    public void testExternalNitriteId() {
        ObjectRepository<WithNitriteId> repository = db.getRepository(WithNitriteId.class);
        WithNitriteId obj = new WithNitriteId();
        NitriteId id = NitriteId.createId("1");
        obj.setIdField(id);
        obj.setName("testExternalNitriteId");
        WriteResult result = repository.update(obj, true);

        obj = new WithNitriteId();
        id = result.iterator().next();
        obj.setIdField(id);
        obj.setName("testExternalNitriteId");
        result = repository.update(obj, true);
        assertNotEquals(id.getIdValue(), result.iterator().next().getIdValue());
    }

    @Test(expected = IndexingException.class)
    public void testWithoutEmbeddedId() {
        ObjectRepository<WithoutEmbeddedId> repository = db.getRepository(WithoutEmbeddedId.class);
        assertNull(repository);
    }

    @Test(expected = InvalidIdException.class)
    public void testGetByWrongIdType() {
        ObjectRepository<WithPublicField> repository = db.getRepository(WithPublicField.class);
        WithPublicField object = new WithPublicField();
        object.name = "test";
        object.number = 2;

        repository.insert(object);

        NitriteId id = NitriteId.createId("1");
        WithPublicField instance = repository.getById(id);
        assertEquals(object.name, instance.name);
        assertEquals(object.number, instance.number);
    }
}
