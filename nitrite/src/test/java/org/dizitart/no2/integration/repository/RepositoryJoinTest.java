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

package org.dizitart.no2.integration.repository;

import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.SimpleDocumentMapper;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.collection.FindOptions.skipBy;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
@RunWith(value = Parameterized.class)
public class RepositoryJoinTest {
    @Parameterized.Parameter
    public boolean isProtected = false;

    protected Nitrite db;
    private ObjectRepository<Person> personRepository;
    private ObjectRepository<Address> addressRepository;

    @Rule
    public Retry retry = new Retry(3);

    @Parameterized.Parameters(name = "Protected = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {false},
            {true},
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
        NitriteBuilder nitriteBuilder = Nitrite.builder()
            .fieldSeparator(".");

        if (isProtected) {
            db = nitriteBuilder.openOrCreate("test-user1", "test-password1");
        } else {
            db = nitriteBuilder.openOrCreate();
        }

        SimpleDocumentMapper documentMapper = (SimpleDocumentMapper) db.getConfig().nitriteMapper();
        documentMapper.registerEntityConverter(new Person.Converter());
        documentMapper.registerEntityConverter(new Address.Converter());
        documentMapper.registerEntityConverter(new PersonDetails.Converter());
    }

    @After
    public void clear() throws Exception {
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

        result = personRepository.find(skipBy(0).limit(5)).join(addressRepository.find(), lookup,
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

        public static class Converter implements EntityConverter<Person> {

            @Override
            public Class<Person> getEntityType() {
                return Person.class;
            }

            @Override
            public Document toDocument(Person entity, NitriteMapper nitriteMapper) {
                return createDocument()
                    .put("nitriteId", entity.nitriteId)
                    .put("id", entity.id)
                    .put("name", entity.name);
            }

            @Override
            public Person fromDocument(Document document, NitriteMapper nitriteMapper) {
                Person entity = new Person();
                entity.nitriteId = document.get("nitriteId", NitriteId.class);
                entity.id = document.get("id", String.class);
                entity.name = document.get("name", String.class);
                return entity;
            }
        }
    }

    @Data
    public static class Address {
        @Id
        private NitriteId nitriteId;
        private String personId;
        private String street;

        public static class Converter implements EntityConverter<Address> {

            @Override
            public Class<Address> getEntityType() {
                return Address.class;
            }

            @Override
            public Document toDocument(Address entity, NitriteMapper nitriteMapper) {
                return createDocument()
                    .put("nitriteId", entity.nitriteId)
                    .put("personId", entity.personId)
                    .put("street", entity.street);
            }

            @Override
            public Address fromDocument(Document document, NitriteMapper nitriteMapper) {
                Address entity = new Address();
                entity.nitriteId = document.get("nitriteId", NitriteId.class);
                entity.personId = document.get("personId", String.class);
                entity.street = document.get("street", String.class);
                return entity;
            }
        }
    }

    @Data
    public static class PersonDetails {
        @Id
        private NitriteId nitriteId;
        private String id;
        private String name;
        private List<Address> addresses;

        public static class Converter implements EntityConverter<PersonDetails> {

            @Override
            public Class<PersonDetails> getEntityType() {
                return PersonDetails.class;
            }

            @Override
            public Document toDocument(PersonDetails entity, NitriteMapper nitriteMapper) {
                List<Document> documents = new ArrayList<>();
                if (entity.addresses != null) {
                    for (Address address : entity.addresses) {
                        documents.add((Document) nitriteMapper.tryConvert(address, Document.class));
                    }
                }

                return createDocument()
                    .put("nitriteId", entity.nitriteId)
                    .put("personId", entity.id)
                    .put("street", entity.name)
                    .put("addresses", documents);
            }

            @Override
            public PersonDetails fromDocument(Document document, NitriteMapper nitriteMapper) {
                PersonDetails entity = new PersonDetails();

                entity.nitriteId = document.get("nitriteId", NitriteId.class);
                entity.id = document.get("id", String.class);
                entity.name = document.get("name", String.class);

                Collection<Document> documents = document.get("addresses", Collection.class);
                if (documents != null) {
                    entity.addresses = new ArrayList<>();
                    for (Document doc : documents) {
                        Address address = (Address) nitriteMapper.tryConvert(doc, Address.class);
                        entity.addresses.add(address);
                    }
                }

                return entity;
            }
        }
    }
}
