/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.objects;

import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Lookup;
import org.dizitart.no2.collection.RecordIterable;
import org.dizitart.no2.index.annotations.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.filters.ObjectFilters.ALL;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
@RunWith(value = Parameterized.class)
public class RepositoryJoinTest {
    private String fileName = getRandomTempDbFile();
    protected Nitrite db;
    private ObjectRepository<Person> personRepository;
    private ObjectRepository<Address> addressRepository;

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

    @Parameterized.Parameters(name = "InMemory = {0}, Protected = {1}, " +
            "Compressed = {2}, AutoCommit = {3}, AutoCompact = {4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
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
        NitriteBuilder builder = Nitrite.builder();

        if (!isAutoCommit) {
            builder.disableAutoCommit();
        }

        if (!inMemory) {
            builder.filePath(fileName);
        }

        if (isCompressed) {
            builder.compressed();
        }

        if (!isAutoCompact) {
            builder.disableAutoCompact();
        }

        if (isProtected) {
            db = builder.openOrCreate("test-user1", "test-password1");
        } else {
            db = builder.openOrCreate();
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
            Files.delete(Paths.get(fileName));
        }
    }

    @Test
    public void testJoin() {
        Lookup lookup = new Lookup();
        lookup.setLocalField("id");
        lookup.setForeignField("personId");
        lookup.setTargetField("addresses");

        RecordIterable<PersonDetails> result
                = personRepository.find().join(addressRepository.find(), lookup,
                PersonDetails.class);
        assertEquals(result.size(), 10);

        for (PersonDetails personDetails: result) {
            Address[] addresses = personDetails.addresses.toArray(new Address[0]);
            if (personDetails.id.equals("5")) {
                assertEquals(addresses.length, 2);
            } else {
                assertEquals(addresses.length, 1);
                assertEquals(addresses[0].personId,  personDetails.getId());
            }
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
