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

import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.mapper.jackson.JacksonMapperModule;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.mvstore.MVStoreModuleBuilder;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.dizitart.no2.filters.Filter.ALL;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
@RunWith(value = Parameterized.class)
public class RepositoryJoinTest {
    @Parameterized.Parameter
    public boolean inMemory = false;
    @Parameterized.Parameter(value = 1)
    public boolean isProtected = false;
    @Parameterized.Parameter(value = 2)
    public boolean isCompressed = false;
    @Parameterized.Parameter(value = 3)
    public boolean isAutoCommit = false;
    @Parameterized.Parameter(value = 4)
    public boolean isAutoCompact = false;
    protected Nitrite db;
    private final String fileName = BaseObjectRepositoryTest.getRandomTempDbFile();
    private ObjectRepository<Person> personRepository;
    private ObjectRepository<Address> addressRepository;

    @Rule
    public Retry retry = new Retry(3);

    @Parameterized.Parameters(name = "InMemory = {0}, Protected = {1}, " +
        "Compressed = {2}, AutoCommit = {3}, AutoCompact = {4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {false, false, false, false, false},
            {false, false, false, true, false},
            {false, false, true, false, false},
            {false, false, true, true, false},
            {false, true, false, false, false},
            {false, true, false, true, false},
            {false, true, true, false, false},
            {false, true, true, true, true},
            {true, false, false, false, true},
            {true, false, false, true, true},
            {true, false, true, false, true},
            {true, false, true, true, true},
            {true, true, false, false, true},
            {true, true, false, true, true},
            {true, true, true, false, true},
            {true, true, true, true, true},
        });
    }

    @Before
    public void setUp() {
        openDb();

        personRepository = db.getRepository(Person.class);
        addressRepository = db.getRepository(Address.class);

        for (int i = 0; i < 10; i++) {
            Person person = new Person();
            person.setId(Integer.toString(i));
            person.setName("Person " + i);
            personRepository.insert(person);

            Address address = new Address();
            address.setPersonId(Integer.toString(i));
            address.setStreet("Street address " + i);
            addressRepository.insert(address);

            if (i == 5) {
                Address address2 = new Address();
                address2.setPersonId(Integer.toString(i));
                address2.setStreet("Street address 2nd " + i);
                addressRepository.insert(address2);
            }
        }
    }

    private void openDb() {
        MVStoreModuleBuilder builder = MVStoreModule.withConfig();

        if (isCompressed) {
            builder.compress(true);
        }

        if (!isAutoCommit) {
            builder.autoCommit(false);
        }

        if (!inMemory) {
            builder.filePath(fileName);
        }

        MVStoreModule storeModule = builder.build();
        NitriteBuilder nitriteBuilder = Nitrite.builder()
            .fieldSeparator(".")
            .loadModule(storeModule);

        nitriteBuilder.loadModule(new JacksonMapperModule());

        if (isProtected) {
            db = nitriteBuilder.openOrCreate("test-user1", "test-password1");
        } else {
            db = nitriteBuilder.openOrCreate();
        }
    }

    @After
    public void clear() throws IOException {
        if (personRepository != null && !personRepository.isDropped()) {
            personRepository.remove(ALL);
        }

        if (addressRepository != null && !addressRepository.isDropped()) {
            addressRepository.remove(ALL);
        }

        if (db != null) {
            db.commit();
            db.close();
        }

        if (!inMemory) {
            TestUtil.deleteDb(fileName);
        }
    }

    @Test
    public void testJoin() {
        Lookup lookup = new Lookup();
        lookup.setLocalField("id");
        lookup.setForeignField("personId");
        lookup.setTargetField("addresses");

        RecordStream<PersonDetails> result
            = personRepository.find().join(addressRepository.find(), lookup,
            PersonDetails.class);
        assertEquals(result.size(), 10);

        for (PersonDetails personDetails : result) {
            Address[] addresses = personDetails.addresses.toArray(new Address[0]);
            if (personDetails.id.equals("5")) {
                assertEquals(addresses.length, 2);
            } else {
                assertEquals(addresses.length, 1);
                assertEquals(addresses[0].personId, personDetails.getId());
            }
        }

        result = personRepository.find(FindOptions.skipBy(0).limit(5)).join(addressRepository.find(), lookup,
            PersonDetails.class);

        assertEquals(result.size(), 5);
        assertFalse(result.isEmpty());
        assertNotNull(result.toString());
    }

    @Test(expected = InvalidOperationException.class)
    public void testRemove() {
        Lookup lookup = new Lookup();
        lookup.setLocalField("id");
        lookup.setForeignField("personId");
        lookup.setTargetField("addresses");

        RecordStream<PersonDetails> result
            = personRepository.find().join(addressRepository.find(), lookup,
            PersonDetails.class);
        assertEquals(result.size(), 10);

        Iterator<PersonDetails> iterator = result.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    @Data
    public static class Person {
        @Id
        private NitriteId nitriteId;
        private String id;
        private String name;
    }

    @Data
    public static class Address {
        @Id
        private NitriteId nitriteId;
        private String personId;
        private String street;
    }

    @Data
    public static class PersonDetails {
        @Id
        private NitriteId nitriteId;
        private String id;
        private String name;
        private List<Address> addresses;
    }
}
