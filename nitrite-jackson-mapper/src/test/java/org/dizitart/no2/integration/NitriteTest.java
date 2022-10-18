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

package org.dizitart.no2.integration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.mapper.JacksonMapperModule;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.integration.repository.Retry;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.Indices;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Set;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;
import static org.dizitart.no2.common.Constants.META_MAP_NAME;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.integration.repository.TestUtil.createDb;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteTest {
    @Rule
    public Retry retry = new Retry(3);
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() throws ParseException {
        db = createDb(new JacksonMapperModule());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

        Document doc1 = createDocument("firstName", "fn1")
            .put("lastName", "ln1")
            .put("birthDay", simpleDateFormat.parse("2012-07-01T16:02:48.440Z"))
            .put("data", new byte[]{1, 2, 3})
            .put("body", "a quick brown fox jump over the lazy dog");
        Document doc2 = createDocument("firstName", "fn2")
            .put("lastName", "ln2")
            .put("birthDay", simpleDateFormat.parse("2010-06-12T16:02:48.440Z"))
            .put("data", new byte[]{3, 4, 3})
            .put("body", "hello world from nitrite");
        Document doc3 = createDocument("firstName", "fn3")
            .put("lastName", "ln2")
            .put("birthDay", simpleDateFormat.parse("2014-04-17T16:02:48.440Z"))
            .put("data", new byte[]{9, 4, 8})
            .put("body", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed nunc mi, mattis ullamcorper dignissim vitae, condimentum non lorem.");

        collection = db.getCollection("test");
        collection.remove(ALL);

        collection.createIndex(IndexOptions.indexOptions(IndexType.FULL_TEXT), "body");
        collection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "firstName");
        collection.insert(doc1, doc2, doc3);
    }

    @After
    public void tearDown() {
        if (collection.isOpen()) {
            collection.remove(ALL);
            collection.close();
        }
        if (db != null && !db.isClosed()) {
            try {
                db.close();
            } catch (NitriteIOException ignore) {
            }
        }
    }

    @Test
    public void testListCollectionNames() {
        Set<String> collectionNames = db.listCollectionNames();
        assertEquals(collectionNames.size(), 1);
    }

    @Test(expected = ValidationException.class)
    public void testListRepositories() {
        db.getRepository(EmptyClass.class);
    }

    @Test
    public void testListRepositories2() {
        db.getRepository(Receipt.class);
        Set<String> repositories = db.listRepositories();
        assertEquals(repositories.size(), 1);
    }

    @Test
    public void testHasCollection() {
        assertTrue(db.hasCollection("test"));
        assertFalse(db.hasCollection("lucene" + INTERNAL_NAME_SEPARATOR + "test"));
    }

    @Test(expected = ValidationException.class)
    public void testHasRepository() {
        db.getRepository(EmptyClass.class);
    }

    @Test
    public void testHasRepository2() {
        db.getRepository(Receipt.class);
        assertTrue(db.hasRepository(Receipt.class));
        assertFalse(db.hasRepository(String.class));
    }

    @Test
    public void testClose() {
        NitriteCollection testCollection = db.getCollection("test");
        testCollection.insert(createDocument("a", "b"));
        db.close();

        assertFalse(testCollection.isOpen());
    }

    @Test
    public void testGetCollection() {
        NitriteCollection collection = db.getCollection("test-collection");
        assertNotNull(collection);
        assertEquals(collection.getName(), "test-collection");
    }

    @Test(expected = ValidationException.class)
    public void testGetRepository() {
        ObjectRepository<EmptyClass> repository = db.getRepository(EmptyClass.class);
    }

    @Test
    public void testGetRepository2() {
        ObjectRepository<Receipt> repository = db.getRepository(Receipt.class);
        assertNotNull(repository);
        assertEquals(repository.getType(), Receipt.class);
    }

    @Test(expected = ValidationException.class)
    public void testGetRepositoryWithKey() {
        ObjectRepository<EmptyClass> repository = db.getRepository(EmptyClass.class, "key");
    }

    @Test
    public void testGetRepositoryWithKey2() {
        ObjectRepository<Receipt> repository = db.getRepository(Receipt.class, "key");
        assertNotNull(repository);
        assertEquals(repository.getType(), Receipt.class);
        assertFalse(db.hasRepository(Receipt.class));
        assertTrue(db.hasRepository(Receipt.class, "key"));
    }

    @Test
    public void testMultipleGetCollection() {
        NitriteCollection collection = db.getCollection("test-collection");
        assertNotNull(collection);
        assertEquals(collection.getName(), "test-collection");

        NitriteCollection collection2 = db.getCollection("test-collection");
        assertNotNull(collection2);
        assertEquals(collection2.getName(), "test-collection");
    }

    @Test
    public void testMultipleGetRepository() {
        ObjectRepository<Receipt> repository = db.getRepository(Receipt.class);
        assertNotNull(repository);
        assertEquals(repository.getType(), Receipt.class);

        ObjectRepository<Receipt> repository2 = db.getRepository(Receipt.class);
        assertNotNull(repository2);
        assertEquals(repository2.getType(), Receipt.class);
    }

    @Test(expected = ValidationException.class)
    public void testGetRepositoryInvalid() {
        db.getRepository((Class<Object>) null);
    }

    @Test(expected = NitriteIOException.class)
    public void testGetCollectionNullStore() {
        db = Nitrite.builder().openOrCreate();
        db.close();
        db.getCollection("test");
    }

    @Test(expected = NitriteIOException.class)
    public void testGetRepositoryNullStore() {
        db = Nitrite.builder().openOrCreate();
        db.close();
        db.getRepository(NitriteTest.class);
    }

    @Test(expected = NitriteIOException.class)
    public void testGetKeyedRepositoryNullStore() {
        db = Nitrite.builder().openOrCreate();
        db.close();
        db.getRepository(NitriteTest.class, "key");
    }

    @Test(expected = NitriteIOException.class)
    public void testCommitNullStore() {
        db = Nitrite.builder().openOrCreate();
        db.close();
        db.commit();
    }

    @Test(expected = ValidationException.class)
    public void testGetCollectionInvalidName() {
        db.getCollection(META_MAP_NAME);
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Indices({
        @Index(value = "synced", type = IndexType.NON_UNIQUE)
    })
    public static class Receipt {
        @Id
        private String clientRef;
        private Boolean synced;
        private Long createdTimestamp = System.currentTimeMillis();
        private Status status;

        public enum Status {
            COMPLETED,
            PREPARING,
        }
    }

    public static class EmptyClass {
    }
}
