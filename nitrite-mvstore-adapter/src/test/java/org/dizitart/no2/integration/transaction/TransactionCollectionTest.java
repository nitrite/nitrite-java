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

package org.dizitart.no2.integration.transaction;

import com.github.javafaker.Faker;
import org.dizitart.no2.integration.collection.BaseCollectionTest;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.TransactionException;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.transaction.Session;
import org.dizitart.no2.transaction.Transaction;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class TransactionCollectionTest extends BaseCollectionTest {

    @Test
    public void testCommitInsert() {
        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                NitriteCollection txCol = transaction.getCollection("test");

                Document document = createDocument("firstName", "John");
                txCol.insert(document);

                assertEquals(txCol.find(where("firstName").eq("John")).size(), 1);
                assertNotEquals(collection.find(where("firstName").eq("John")).size(), 1);

                transaction.commit();

                assertEquals(collection.find(where("firstName").eq("John")).size(), 1);
            }
        }
    }

    @Test
    public void testRollbackInsert() {
        collection.createIndex("firstName");
        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                NitriteCollection txCol = transaction.getCollection("test");

                Document document = createDocument("firstName", "John");
                Document document2 = createDocument("firstName", "Jane").put("lastName", "Doe");
                txCol.insert(document);
                txCol.insert(document2);

                // just to create UniqueConstraintViolation for rollback
                collection.insert(createDocument("firstName", "Jane"));

                assertEquals(txCol.find(where("firstName").eq("John")).size(), 1);
                assertNotEquals(collection.find(where("lastName").eq("Doe")).size(), 1);

                transaction.commit();
                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertNotEquals(collection.find(where("firstName").eq("John")).size(), 1);
                assertNotEquals(collection.find(where("lastName").eq("Doe")).size(), 1);
            }
        }
    }

    @Test
    public void testCommitUpdate() {
        Document document = createDocument("firstName", "John");
        collection.insert(document);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                NitriteCollection txCol = transaction.getCollection("test");
                document.put("lastName", "Doe");

                txCol.update(where("firstName").eq("John"), document, updateOptions(true));

                assertEquals(txCol.find(where("lastName").eq("Doe")).size(), 1);
                assertNotEquals(collection.find(where("lastName").eq("Doe")).size(), 1);

                transaction.commit();

                assertEquals(collection.find(where("lastName").eq("Doe")).size(), 1);
            }
        }
    }

    @Test
    public void testRollbackUpdate() {
        collection.createIndex("firstName");
        collection.insert(createDocument("firstName", "Jane"));

        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                NitriteCollection txCol = transaction.getCollection("test");

                Document document = createDocument("firstName", "John");
                Document document2 = createDocument("firstName", "Jane").put("lastName", "Doe");
                txCol.update(where("firstName").eq("Jane"), document2);
                txCol.insert(document);

                // just to create UniqueConstraintViolation for rollback
                collection.insert(createDocument("firstName", "John"));

                assertEquals(txCol.find(where("firstName").eq("John")).size(), 1);
                assertEquals(txCol.find(where("lastName").eq("Doe")).size(), 1);
                assertNotEquals(collection.find(where("lastName").eq("Doe")).size(), 1);

                transaction.commit();
                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertEquals(collection.find(where("firstName").eq("Jane")).size(), 1);
                assertNotEquals(collection.find(where("lastName").eq("Doe")).size(), 1);
            }
        }
    }

    @Test
    public void testCommitRemove() {
        Document document = createDocument("firstName", "John");
        collection.insert(document);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                NitriteCollection txCol = transaction.getCollection("test");

                txCol.remove(where("firstName").eq("John"));

                assertEquals(txCol.find(where("firstName").eq("John")).size(), 0);
                assertEquals(collection.find(where("firstName").eq("John")).size(), 1);

                transaction.commit();

                assertEquals(collection.find(where("firstName").eq("John")).size(), 0);
            }
        }
    }

    @Test
    public void testRollbackRemove() {
        collection.createIndex("firstName");
        Document document = createDocument("firstName", "John");
        collection.insert(document);

        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                NitriteCollection txCol = transaction.getCollection("test");

                txCol.remove(where("firstName").eq("John"));

                assertEquals(txCol.find(where("firstName").eq("John")).size(), 0);
                assertEquals(collection.find(where("firstName").eq("John")).size(), 1);

                txCol.insert(createDocument("firstName", "Jane"));
                collection.insert(createDocument("firstName", "Jane"));

                transaction.commit();

                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertEquals(collection.find(where("firstName").eq("John")).size(), 1);
                assertEquals(collection.find(where("firstName").eq("Jane")).size(), 1);
            }
        }
    }

    @Test
    public void testCommitCreateIndex() {
        Document document = createDocument("firstName", "John");
        collection.insert(document);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                NitriteCollection txCol = transaction.getCollection("test");
                txCol.createIndex(indexOptions(IndexType.FULL_TEXT), "firstName");

                assertTrue(txCol.hasIndex("firstName"));
                assertFalse(collection.hasIndex("firstName"));

                transaction.commit();

                assertTrue(collection.hasIndex("firstName"));
            }
        }
    }

    @Test
    public void testRollbackCreateIndex() {
        Document document = createDocument("firstName", "John");
        Document document2 = createDocument("firstName", "Jane");
        collection.insert(document);

        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                NitriteCollection txCol = transaction.getCollection("test");

                txCol.createIndex("firstName");

                assertTrue(txCol.hasIndex("firstName"));
                assertFalse(collection.hasIndex("firstName"));

                txCol.insert(document2);
                collection.insert(document2);

                transaction.commit();

                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertFalse(collection.hasIndex("firstName"));
            }
        }
    }

    @Test
    public void testCommitClear() {
        Document document = createDocument("firstName", "John");
        collection.insert(document);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                NitriteCollection txCol = transaction.getCollection("test");
                txCol.clear();

                assertEquals(0, txCol.size());
                assertEquals(1, collection.size());

                transaction.commit();

                assertEquals(0, collection.size());
            }
        }
    }

    @Test
    public void testRollbackClear() {
        collection.createIndex("firstName");
        Document document = createDocument("firstName", "John");
        Document document2 = createDocument("firstName", "Jane");
        collection.insert(document);

        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                NitriteCollection txCol = transaction.getCollection("test");

                txCol.clear();

                assertEquals(0, txCol.size());
                assertEquals(1, collection.size());

                txCol.insert(document2);
                collection.insert(document2);

                transaction.commit();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertEquals(0, collection.size());
            }
        }
    }

    @Test
    public void testCommitDropIndex() {
        Document document = createDocument("firstName", "John");
        collection.insert(document);
        collection.createIndex("firstName");

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                NitriteCollection txCol = transaction.getCollection("test");
                txCol.dropIndex("firstName");

                assertFalse(txCol.hasIndex("firstName"));
                assertTrue(collection.hasIndex("firstName"));

                transaction.commit();

                assertFalse(collection.hasIndex("firstName"));
            }
        }
    }

    @Test
    public void testRollbackDropIndex() {
        Document document = createDocument("firstName", "John").put("lastName", "Doe");
        Document document2 = createDocument("firstName", "Jane").put("lastName", "Doe");
        collection.insert(document);
        collection.createIndex("firstName");
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName");

        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                NitriteCollection txCol = transaction.getCollection("test");
                txCol.dropIndex("lastName");

                assertFalse(txCol.hasIndex("lastName"));
                assertTrue(collection.hasIndex("lastName"));

                txCol.insert(document2);
                collection.insert(document2);

                transaction.commit();

                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertTrue(collection.hasIndex("lastName"));
            }
        }
    }

    @Test
    public void testCommitDropAllIndices() {
        Document document = createDocument("firstName", "John");
        collection.insert(document);
        collection.createIndex("firstName");
        collection.createIndex("lastName");

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                NitriteCollection txCol = transaction.getCollection("test");
                txCol.dropAllIndices();

                assertFalse(txCol.hasIndex("firstName"));
                assertFalse(txCol.hasIndex("lastName"));
                assertTrue(collection.hasIndex("firstName"));
                assertTrue(collection.hasIndex("lastName"));

                transaction.commit();

                assertFalse(collection.hasIndex("firstName"));
                assertFalse(collection.hasIndex("lastName"));
            }
        }
    }

    @Test
    public void testRollbackDropAllIndices() {
        Document document = createDocument("firstName", "John").put("lastName", "Doe");
        collection.insert(document);
        collection.createIndex("firstName");
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName");

        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                NitriteCollection txCol = transaction.getCollection("test");
                txCol.dropAllIndices();

                assertFalse(txCol.hasIndex("firstName"));
                assertFalse(txCol.hasIndex("lastName"));
                assertTrue(collection.hasIndex("firstName"));
                assertTrue(collection.hasIndex("lastName"));

                txCol.insert(createDocument("firstName", "Jane").put("lastName", "Doe"));
                collection.insert(createDocument("firstName", "Jane").put("lastName", "Doe"));

                throw new TransactionException("failed");
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertTrue(collection.hasIndex("firstName"));
                assertTrue(collection.hasIndex("lastName"));
            }
        }
    }

    @Test
    public void testCommitDropCollection() {
        Document document = createDocument("firstName", "John");
        collection.insert(document);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                NitriteCollection txCol = transaction.getCollection("test");
                txCol.drop();

                boolean expectedException = false;
                try {
                    assertEquals(0, txCol.size());
                } catch (TransactionException e) {
                    expectedException = true;
                }
                assertTrue(expectedException);
                assertEquals(1, collection.size());

                transaction.commit();

                expectedException = false;
                try {
                    assertEquals(0, collection.size());
                } catch (NitriteIOException e) {
                    expectedException = true;
                }
                assertTrue(expectedException);
            }
        }
    }

    @Test
    public void testRollbackDropCollection() {
        collection.createIndex("firstName");
        Document document = createDocument("firstName", "John");
        collection.insert(document);

        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                NitriteCollection txCol = transaction.getCollection("test");

                txCol.drop();

                boolean expectedException = false;
                try {
                    assertEquals(0, txCol.size());
                } catch (NitriteIOException e) {
                    expectedException = true;
                }
                assertTrue(expectedException);
                assertEquals(1, collection.size());

                throw new TransactionException("failed");
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertEquals(1, collection.size());
            }
        }
    }

    @Test
    public void testCommitSetAttribute() {
        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                NitriteCollection txCol = transaction.getCollection("test");

                Attributes attributes = new Attributes();
                Map<String, String> hashMap = new HashMap<>();
                hashMap.put("key", "value");
                attributes.setAttributes(hashMap);
                txCol.setAttributes(attributes);

                assertNull(collection.getAttributes());

                transaction.commit();

                assertEquals("value", collection.getAttributes().get("key"));
            }
        }
    }

    @Test
    public void testRollbackSetAttribute() {
        collection.createIndex("firstName");
        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                NitriteCollection txCol = transaction.getCollection("test");

                Attributes attributes = new Attributes();
                Map<String, String> map = new HashMap<>();
                map.put("key", "value");
                attributes.setAttributes(map);
                txCol.setAttributes(attributes);

                txCol.insert(createDocument("firstName", "John"));
                txCol.insert(createDocument("firstName", "Jane").put("lastName", "Doe"));

                assertNull(collection.getAttributes());

                // just to create UniqueConstraintViolation for rollback
                collection.insert(createDocument("firstName", "Jane"));

                assertEquals(txCol.find(where("firstName").eq("John")).size(), 1);
                assertNotEquals(collection.find(where("lastName").eq("Doe")).size(), 1);

                transaction.commit();
                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertNull(collection.getAttributes().get("key"));
            }
        }
    }

    @Test
    public void testConcurrentInsertAndRemove() {
        NitriteCollection collection = db.getCollection("test");
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "firstName");
        collection.createIndex("id");
        Faker faker = new Faker();
        List<Future<?>> futures = new ArrayList<>();

        try (Session session = db.createSession()) {
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (int i = 0; i < 10; i++) {
                final long fi = i;
                Future<?> future = executorService.submit(() -> {
                    Transaction transaction = session.beginTransaction();
                    try {
                        NitriteCollection txCol = transaction.getCollection("test");

                        for (int j = 0; j < 10; j++) {
                            Document document = createDocument("firstName", faker.name().firstName())
                                .put("lastName", faker.name().lastName())
                                .put("id", j + (fi * 10));
                            txCol.insert(document);
                        }

                        txCol.remove(where("id").eq(2 + (fi * 10)));

                        transaction.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        transaction.rollback();
                    } finally {
                        transaction.close();
                    }
                });

                futures.add(future);
            }

            futures.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });

            assertEquals(90, collection.size());
        }
    }

    @Test
    public void testConcurrentInsert() {
        NitriteCollection collection = db.getCollection("test");
        Faker faker = new Faker();
        List<Future<?>> futures = new ArrayList<>();

        try (Session session = db.createSession()) {
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (int i = 0; i < 10; i++) {
                Future<?> future = executorService.submit(() -> {
                    Transaction transaction = session.beginTransaction();
                    try {
                        NitriteCollection txCol = transaction.getCollection("test");

                        for (int j = 0; j < 10; j++) {
                            Document document = createDocument("firstName", faker.name().firstName())
                                .put("lastName", faker.name().lastName());
                            txCol.insert(document);
                        }

                        transaction.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        transaction.rollback();
                    } finally {
                        transaction.close();
                    }
                });

                futures.add(future);
            }

            futures.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });

            assertEquals(100, collection.size());
        }
    }

    @Test
    public void testConcurrentUpdate() {
        NitriteCollection collection = db.getCollection("test");
        for (int i = 0; i < 10; i++) {
            Document document = createDocument("id", i);
            collection.insert(document);
        }

        Faker faker = new Faker();
        List<Future<?>> futures = new ArrayList<>();
        try (Session session = db.createSession()) {
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (int i = 0; i < 10; i++) {
                Future<?> future = executorService.submit(() -> {
                    Transaction transaction = session.beginTransaction();
                    try {
                        NitriteCollection txCol = transaction.getCollection("test");

                        for (int j = 0; j < 10; j++) {
                            Document document = createDocument("firstName", faker.name().firstName())
                                .put("lastName", faker.name().lastName())
                                .put("id", j);
                            txCol.update(where("id").eq(j), document, updateOptions(true));
                        }

                        transaction.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        transaction.rollback();
                    } finally {
                        transaction.close();
                    }
                });

                futures.add(future);
            }

            futures.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });

            assertEquals(10, collection.size());
        }
    }

    @Test
    public void testTransactionOnDifferentCollections() {
        NitriteCollection col1 = db.getCollection("test1");
        NitriteCollection col2 = db.getCollection("test2");
        NitriteCollection col3 = db.getCollection("test3");
        col3.createIndex("id");

        Faker faker = new Faker();

        try(Session session = db.createSession()) {
            Transaction transaction = session.beginTransaction();

            NitriteCollection test1 = transaction.getCollection("test1");
            NitriteCollection test2 = transaction.getCollection("test2");
            NitriteCollection test3 = transaction.getCollection("test3");

            for (int i = 0; i < 10; i++) {
                Document document = createDocument("firstName", faker.name().firstName())
                    .put("id", i);
                test1.insert(document);

                document = createDocument("firstName", faker.name().firstName())
                    .put("id", i + 10);
                test2.insert(document);

                document = createDocument("firstName", faker.name().firstName())
                    .put("id", i + 20);
                test3.insert(document);
            }

            assertEquals(test1.size(), 10);
            assertEquals(test2.size(), 10);
            assertEquals(test3.size(), 10);

            assertEquals(col1.size(), 0);
            assertEquals(col2.size(), 0);
            assertEquals(col3.size(), 0);

            transaction.commit();

            assertEquals(col1.size(), 10);
            assertEquals(col2.size(), 10);
            assertEquals(col3.size(), 10);
        }


        Transaction transaction = null;
        try (Session session = db.createSession()) {
            transaction = session.beginTransaction();

            NitriteCollection test1 = transaction.getCollection("test1");
            NitriteCollection test2 = transaction.getCollection("test2");
            NitriteCollection test3 = transaction.getCollection("test3");

            for (int i = 0; i < 10; i++) {
                Document document = createDocument("firstName", faker.name().firstName())
                    .put("id", i + 30);
                test1.insert(document);

                document = createDocument("firstName", faker.name().firstName())
                    .put("id", i + 40);
                test2.insert(document);

                document = createDocument("firstName", faker.name().firstName())
                    .put("id", i + 50);
                test3.insert(document);
            }

            assertEquals(test1.size(), 20);
            assertEquals(test2.size(), 20);
            assertEquals(test3.size(), 20);

            assertEquals(col1.size(), 10);
            assertEquals(col2.size(), 10);
            assertEquals(col3.size(), 10);

            Document document = createDocument("firstName", faker.name().firstName())
                .put("id", 52);
            col3.insert(document);

            transaction.commit();

            fail();
        } catch (TransactionException e) {
            assert transaction != null;
            transaction.rollback();

            assertEquals(col1.size(), 10);
            assertEquals(col2.size(), 10);
            assertEquals(col3.size(), 11); // last document added
        }
    }

    @Test(expected = TransactionException.class)
    public void testFailureOnClosedTransaction() {
        try(Session session = db.createSession()) {
            Transaction transaction = session.beginTransaction();
            NitriteCollection col = transaction.getCollection("test");
            col.insert(createDocument("id", 1));
            transaction.commit();

            col.insert(createDocument("id", 2));
            fail();
        }
    }
}
