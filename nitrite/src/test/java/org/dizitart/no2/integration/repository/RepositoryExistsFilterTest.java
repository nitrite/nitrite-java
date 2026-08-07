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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryExistsFilterTest {
    private Nitrite db;
    private ObjectRepository<Contact> repository;

    @Before
    public void setUp() {
        db = Nitrite.builder()
            .fieldSeparator(".")
            .registerEntityConverter(new Contact.ContactConverter())
            .openOrCreate();
        repository = db.getRepository(Contact.class);

        repository.insert(new Contact(1L, "a", "aa"));
        repository.insert(new Contact(2L, "b", null));
        repository.insert(new Contact(3L, "c", "cc"));
    }

    @After
    public void tearDown() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }

    private List<String> names(Iterable<Contact> cursor) {
        List<String> names = new ArrayList<>();
        for (Contact contact : cursor) {
            names.add(contact.getName());
        }
        return names;
    }

    @Test
    public void testExists() {
        assertEquals(List.of("a", "c"), names(repository.find(where("nick").exists())));
    }

    @Test
    public void testNotExists() {
        assertEquals(List.of("b"), names(repository.find(where("nick").exists().not())));
    }

    @Test
    public void testExistsOnIdField() {
        assertEquals(List.of("a", "b", "c"), names(repository.find(where("id").exists())));
    }

    @Test
    public void testExistsOnUnknownField() {
        assertEquals(0, repository.find(where("unknown").exists()).size());
    }

    @Data
    @Entity
    public static class Contact {
        @Id
        private Long id;
        private String name;
        private String nick;

        public Contact() {
        }

        public Contact(Long id, String name, String nick) {
            this.id = id;
            this.name = name;
            this.nick = nick;
        }

        public static class ContactConverter implements EntityConverter<Contact> {
            @Override
            public Class<Contact> getEntityType() {
                return Contact.class;
            }

            @Override
            public Document toDocument(Contact entity, NitriteMapper nitriteMapper) {
                Document document = createDocument("id", entity.id)
                    .put("name", entity.name);
                // a null nick is left out of the document altogether, which is
                // exactly the case `exists()` is meant to express
                if (entity.nick != null) {
                    document.put("nick", entity.nick);
                }
                return document;
            }

            @Override
            public Contact fromDocument(Document document, NitriteMapper nitriteMapper) {
                Contact entity = new Contact();
                entity.id = document.get("id", Long.class);
                entity.name = document.get("name", String.class);
                entity.nick = document.get("nick", String.class);
                return entity;
            }
        }
    }
}
