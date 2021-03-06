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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.mapper.Mappable;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.collection.FindOptions.skipBy;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryJoinTest extends BaseObjectRepositoryTest {
    private ObjectRepository<Person> personRepository;
    private ObjectRepository<Address> addressRepository;

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

    @After
    public void clear() throws Exception {
        if (personRepository != null && !personRepository.isDropped()) {
            personRepository.remove(ALL);
        }

        if (addressRepository != null && !addressRepository.isDropped()) {
            addressRepository.remove(ALL);
        }

        super.clear();
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
    public static class Person implements Mappable {
        @Id
        private NitriteId nitriteId;
        private String id;
        private String name;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument()
                .put("nitriteId", nitriteId)
                .put("id", id)
                .put("name", name);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            nitriteId = document.get("nitriteId", NitriteId.class);
            id = document.get("id", String.class);
            name = document.get("name", String.class);
        }
    }

    @Data
    public static class Address implements Mappable {
        @Id
        private NitriteId nitriteId;
        private String personId;
        private String street;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument()
                .put("nitriteId", nitriteId)
                .put("personId", personId)
                .put("street", street);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            nitriteId = document.get("nitriteId", NitriteId.class);
            personId = document.get("personId", String.class);
            street = document.get("street", String.class);
        }
    }

    @Data
    public static class PersonDetails implements Mappable {
        @Id
        private NitriteId nitriteId;
        private String id;
        private String name;
        private List<Address> addresses;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument()
                .put("nitriteId", nitriteId)
                .put("personId", id)
                .put("street", name)
                .put("addresses", addresses);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void read(NitriteMapper mapper, Document document) {
            nitriteId = document.get("nitriteId", NitriteId.class);
            id = document.get("id", String.class);
            name = document.get("name", String.class);
            Set<Document> documents = document.get("addresses", Set.class);
            this.addresses = new ArrayList<>();
            for (Document doc : documents) {
                Address address = new Address();
                address.read(mapper, doc);
                addresses.add(address);
            }
        }
    }
}
