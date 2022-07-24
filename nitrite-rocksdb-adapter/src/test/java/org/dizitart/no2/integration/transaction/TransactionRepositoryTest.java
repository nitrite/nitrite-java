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
import org.dizitart.no2.integration.repository.BaseObjectRepositoryTest;
import org.dizitart.no2.integration.repository.data.SubEmployee;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.TransactionException;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.transaction.Session;
import org.dizitart.no2.transaction.Transaction;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class TransactionRepositoryTest extends BaseObjectRepositoryTest {

    @Test
    public void testCommitInsert() {
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);

                TxData txData1 = new TxData();
                txData1.setId(1L);
                txData1.setName("John");

                txRepo.insert(txData1);

                assertEquals(txRepo.find(where("name").eq("John")).size(), 1);
                assertNotEquals(repository.find(where("name").eq("John")).size(), 1);

                transaction.commit();

                assertEquals(repository.find(where("name").eq("John")).size(), 1);
            }
        }
    }

    @Test
    public void testRollbackInsert() {
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.createIndex("name");

        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);

                TxData txData1 = new TxData();
                txData1.setId(1L);
                txData1.setName("John");

                TxData txData2 = new TxData();
                txData2.setId(2L);
                txData2.setName("Jane");

                txRepo.insert(txData1, txData2);

                txData2.setName("Molly");
                repository.insert(txData2);

                assertEquals(txRepo.find(where("name").eq("John")).size(), 1);
                assertEquals(repository.find(where("name").eq("John")).size(), 0);

                transaction.commit();

                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertEquals(repository.find(where("name").eq("John")).size(), 0);
                assertEquals(repository.find(where("name").eq("Molly")).size(), 1);
            }
        }
    }

    @Test
    public void testCommitUpdate() {
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.insert(new TxData(1L, "John"));

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                TxData txData1 = new TxData(1L, "Jane");
                txRepo.update(txData1, true);

                assertEquals(txRepo.find(where("name").eq("Jane")).size(), 1);
                assertNotEquals(repository.find(where("name").eq("Jane")).size(), 1);

                transaction.commit();

                assertEquals(repository.find(where("name").eq("Jane")).size(), 1);
            }
        }
    }

    @Test
    public void testRollbackUpdate() {
        ObjectRepository<TxData> repository = db.getRepository(TxData.class, "rollback");
        repository.createIndex("name");
        repository.insert(new TxData(1L, "Jane"));

        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class, "rollback");

                TxData txData1 = new TxData();
                txData1.setId(2L);
                txData1.setName("John");

                TxData txData2 = new TxData();
                txData2.setId(1L);
                txData2.setName("Jane Doe");
                txRepo.update(txData2);
                txRepo.insert(txData1);

                // just to create UniqueConstraintViolation for rollback
                repository.insert(txData1);

                assertEquals(txRepo.find(where("name").eq("John")).size(), 1);
                assertEquals(txRepo.find(where("name").eq("Jane Doe")).size(), 1);
                assertNotEquals(repository.find(where("name").eq("Jane Doe")).size(), 1);

                transaction.commit();

                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertEquals(repository.find(where("name").eq("Jane")).size(), 1);
                assertNotEquals(repository.find(where("name").eq("Jane Doe")).size(), 1);
            }
        }
    }

    @Test
    public void testCommitRemove() {
        TxData txData1 = new TxData(1L, "John");
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.insert(txData1);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);

                txRepo.remove(where("name").eq("John"));

                assertEquals(txRepo.find(where("name").eq("John")).size(), 0);
                assertEquals(repository.find(where("name").eq("John")).size(), 1);

                transaction.commit();

                assertEquals(repository.find(where("name").eq("John")).size(), 0);
            }
        }
    }

    @Test
    public void testRollbackRemove() {
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.createIndex("name");
        TxData txData1 = new TxData(1L, "John");
        repository.insert(txData1);

        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);

                txRepo.remove(where("name").eq("John"));

                assertEquals(txRepo.find(where("name").eq("John")).size(), 0);
                assertEquals(repository.find(where("name").eq("John")).size(), 1);

                TxData txData2 = new TxData(2L, "Jane");
                txRepo.insert(txData2);
                repository.insert(txData2);

                transaction.commit();

                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertEquals(repository.find(where("name").eq("John")).size(), 1);
                assertEquals(repository.find(where("name").eq("Jane")).size(), 1);
            }
        }
    }

    @Test
    public void testCommitCreateIndex() {
        TxData txData1 = new TxData(1L, "John");
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.insert(txData1);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                txRepo.createIndex(indexOptions(IndexType.FULL_TEXT), "name");

                assertTrue(txRepo.hasIndex("name"));
                assertFalse(repository.hasIndex("name"));

                transaction.commit();

                assertTrue(repository.hasIndex("name"));
            }
        }
    }

    @Test
    public void testRollbackCreateIndex() {
        TxData txData1 = new TxData(1L, "John");
        TxData txData2 = new TxData(2L, "Jane");

        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.insert(txData1);

        try(Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                txRepo.createIndex(indexOptions(IndexType.FULL_TEXT), "name");

                assertTrue(txRepo.hasIndex("name"));
                assertFalse(repository.hasIndex("name"));

                txRepo.insert(txData2);
                repository.insert(txData2);

                transaction.commit();
                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertFalse(repository.hasIndex("name"));
            }
        }
    }

    @Test
    public void testCommitClear() {
        TxData txData1 = new TxData(1L, "John");
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.insert(txData1);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                txRepo.clear();

                assertEquals(0, txRepo.size());
                assertEquals(1, repository.size());

                transaction.commit();

                assertEquals(0, repository.size());
            }
        }
    }

    @Test
    public void testRollbackClear() {
        TxData txData1 = new TxData(1L, "John");
        TxData txData2 = new TxData(2L, "Jane");

        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.createIndex("name");
        repository.insert(txData1);

        try(Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                txRepo.clear();

                assertEquals(0, txRepo.size());
                assertEquals(1, repository.size());

                txRepo.insert(txData2);
                repository.insert(txData2);

                transaction.commit();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertEquals(0, repository.size());
            }
        }
    }

    @Test
    public void testCommitDropIndex() {
        TxData txData1 = new TxData(1L, "John");
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.createIndex("name");
        repository.insert(txData1);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                txRepo.dropIndex("name");

                assertFalse(txRepo.hasIndex("name"));
                assertTrue(repository.hasIndex("name"));

                transaction.commit();

                assertFalse(repository.hasIndex("name"));
            }
        }
    }

    @Test
    public void testRollbackDropIndex() {
        TxData txData1 = new TxData(1L, "John");
        TxData txData2 = new TxData(2L, "Jane");

        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.createIndex("name");
        repository.insert(txData1);

        try(Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                txRepo.dropIndex("name");

                assertFalse(txRepo.hasIndex("name"));
                assertTrue(repository.hasIndex("name"));

                txRepo.insert(txData2);
                repository.insert(txData2);

                transaction.commit();
                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertTrue(repository.hasIndex("name"));
            }
        }
    }

    @Test
    public void testCommitDropAllIndices() {
        TxData txData1 = new TxData(1L, "John");
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.createIndex("name");
        repository.insert(txData1);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                txRepo.dropAllIndices();

                assertFalse(txRepo.hasIndex("name"));
                assertTrue(repository.hasIndex("name"));

                transaction.commit();

                assertFalse(repository.hasIndex("name"));
            }
        }
    }

    @Test
    public void testRollbackDropAllIndices() {
        TxData txData1 = new TxData(1L, "John");
        TxData txData2 = new TxData(2L, "Jane");

        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.createIndex("name");
        repository.insert(txData1);

        try(Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                txRepo.dropAllIndices();

                assertFalse(txRepo.hasIndex("name"));
                assertTrue(repository.hasIndex("name"));

                txRepo.insert(txData2);
                repository.insert(txData2);

                throw new TransactionException("failed");
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertTrue(repository.hasIndex("name"));
            }
        }
    }

    @Test
    public void testCommitDropRepository() {
        TxData txData1 = new TxData(1L, "John");
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.insert(txData1);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                txRepo.drop();

                boolean expectedException = false;
                try {
                    assertEquals(0, txRepo.size());
                } catch (TransactionException e) {
                    expectedException = true;
                }
                assertTrue(expectedException);
                assertEquals(1, repository.size());

                transaction.commit();

                expectedException = false;
                try {
                    assertEquals(0, repository.size());
                } catch (NitriteIOException e) {
                    expectedException = true;
                }
                assertTrue(expectedException);
            }
        }
    }

    @Test
    public void testRollbackDropRepository() {
        TxData txData1 = new TxData(1L, "John");

        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.createIndex("name");
        repository.insert(txData1);

        try(Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
                txRepo.drop();

                boolean expectedException = false;
                try {
                    assertEquals(0, txRepo.size());
                } catch (NitriteIOException e) {
                    expectedException = true;
                }
                assertTrue(expectedException);
                assertEquals(1, repository.size());

                throw new TransactionException("failed");
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertEquals(1, repository.size());
            }
        }
    }

    @Test
    public void testCommitSetAttribute() {
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);

        try (Session session = db.createSession()) {
            try (Transaction transaction = session.beginTransaction()) {
                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);

                Attributes attributes = new Attributes();
                Map<String, String> hashMap = new HashMap<>();
                hashMap.put("key", "value");
                attributes.setAttributes(hashMap);
                txRepo.setAttributes(attributes);

                assertNull(repository.getAttributes());

                transaction.commit();

                assertEquals("value", repository.getAttributes().get("key"));
            }
        }
    }

    @Test
    public void testRollbackSetAttribute() {
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.createIndex("name");
        try (Session session = db.createSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();

                ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);

                Attributes attributes = new Attributes();
                Map<String, String> map = new HashMap<>();
                map.put("key", "value");
                attributes.setAttributes(map);
                txRepo.setAttributes(attributes);

                txRepo.insert(new TxData(1L, "John"));
                txRepo.insert(new TxData(2L, "Jane"));

                assertNull(repository.getAttributes());

                // just to create UniqueConstraintViolation for rollback
                repository.insert(new TxData(2L, "Jane"));

                assertEquals(txRepo.find(where("name").eq("John")).size(), 1);
                assertNotEquals(repository.find(where("name").eq("John")).size(), 1);

                transaction.commit();
                fail();
            } catch (TransactionException e) {
                assert transaction != null;
                transaction.rollback();
                assertNull(repository.getAttributes().get("key"));
            }
        }
    }

    @Test
    public void testConcurrentInsertAndRemove() {
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        repository.createIndex(indexOptions(IndexType.NON_UNIQUE), "name");
        Faker faker = new Faker();
        List<Future<?>> futures = new ArrayList<>();

        try (Session session = db.createSession()) {
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (int i = 0; i < 10; i++) {
                final long fi = i;
                Future<?> future = executorService.submit(() -> {
                    Transaction transaction = session.beginTransaction();
                    try {
                        ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);

                        for (long j = 0; j < 10; j++) {
                            TxData txData = new TxData(j + (fi * 10), faker.name().name());
                            txRepo.insert(txData);
                        }

                        txRepo.remove(where("id").eq(2L + (fi * 10)));

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

            assertEquals(90, repository.size());
        }
    }

    @Test
    public void testConcurrentInsert() {
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        Faker faker = new Faker();
        List<Future<?>> futures = new ArrayList<>();

        try (Session session = db.createSession()) {
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (int i = 0; i < 10; i++) {
                final long fi = i;
                Future<?> future = executorService.submit(() -> {
                    Transaction transaction = session.beginTransaction();
                    try {
                        ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);

                        for (long j = 0; j < 10; j++) {
                            TxData txData = new TxData(j + (fi * 10), faker.name().name());
                            txRepo.insert(txData);
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

            assertEquals(100, repository.size());
        }
    }

    @Test
    public void testConcurrentUpdate() {
        ObjectRepository<TxData> repository = db.getRepository(TxData.class);
        Faker faker = new Faker();
        for (long j = 0; j < 10; j++) {
            TxData txData = new TxData(j, faker.name().name());
            repository.insert(txData);
        }

        List<Future<?>> futures = new ArrayList<>();
        try (Session session = db.createSession()) {
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (int i = 0; i < 10; i++) {
                Future<?> future = executorService.submit(() -> {
                    Transaction transaction = session.beginTransaction();
                    try {
                        ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);

                        for (int j = 0; j < 10; j++) {
                            TxData txData = new TxData((long) j, faker.name().name());
                            txRepo.update(where("id").eq(j), txData);
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

            assertEquals(10, repository.size());
        }
    }

    @Test
    public void testTransactionOnDifferentRepositoriesAndCollections() {
        ObjectRepository<TxData> repo1 = db.getRepository(TxData.class);
        ObjectRepository<TxData> repo2 = db.getRepository(TxData.class, "2");
        ObjectRepository<SubEmployee> repo3 = db.getRepository(SubEmployee.class);
        NitriteCollection col1 = db.getCollection("test1");
        col1.createIndex("id");

        Faker faker = new Faker();

        try(Session session = db.createSession()) {
            Transaction transaction = session.beginTransaction();

            ObjectRepository<TxData> txRepo1 = transaction.getRepository(TxData.class);
            ObjectRepository<TxData> txRepo2 = transaction.getRepository(TxData.class, "2");
            ObjectRepository<SubEmployee> txRepo3 = transaction.getRepository(SubEmployee.class);
            NitriteCollection test1 = transaction.getCollection("test1");

            for (long i = 0; i < 10; i++) {
                Document document = createDocument("firstName", faker.name().firstName())
                    .put("id", i);
                test1.insert(document);

                TxData txData1 = new TxData(i, faker.name().name());
                txRepo1.insert(txData1);

                TxData txData2 = new TxData(i + 10, faker.name().name());
                txRepo2.insert(txData2);

                SubEmployee employee = new SubEmployee();
                employee.setAddress(faker.address().fullAddress());
                employee.setEmpId(i);
                employee.setJoinDate(faker.date().birthday());
                txRepo3.insert(employee);
            }

            assertEquals(test1.size(), 10);
            assertEquals(txRepo1.size(), 10);
            assertEquals(txRepo2.size(), 10);
            assertEquals(txRepo3.size(), 10);

            assertEquals(col1.size(), 0);
            assertEquals(repo1.size(), 0);
            assertEquals(repo2.size(), 0);
            assertEquals(repo3.size(), 0);

            transaction.commit();

            assertEquals(col1.size(), 10);
            assertEquals(repo1.size(), 10);
            assertEquals(repo2.size(), 10);
            assertEquals(repo3.size(), 10);
        }

        Transaction transaction = null;
        try (Session session = db.createSession()) {
            transaction = session.beginTransaction();

            ObjectRepository<TxData> txRepo1 = transaction.getRepository(TxData.class);
            ObjectRepository<TxData> txRepo2 = transaction.getRepository(TxData.class, "2");
            ObjectRepository<SubEmployee> txRepo3 = transaction.getRepository(SubEmployee.class);
            NitriteCollection test1 = transaction.getCollection("test1");

            for (long i = 0; i < 10; i++) {
                Document document = createDocument("firstName", faker.name().firstName())
                    .put("id", i + 10);
                test1.insert(document);

                TxData txData1 = new TxData(i + 10, faker.name().name());
                txRepo1.insert(txData1);

                TxData txData2 = new TxData(i + 20, faker.name().name());
                txRepo2.insert(txData2);

                SubEmployee employee = new SubEmployee();
                employee.setAddress(faker.address().fullAddress());
                employee.setEmpId(i + 10);
                employee.setJoinDate(faker.date().birthday());
                txRepo3.insert(employee);
            }

            assertEquals(test1.size(), 20);
            assertEquals(txRepo1.size(), 20);
            assertEquals(txRepo2.size(), 20);
            assertEquals(txRepo3.size(), 20);

            assertEquals(col1.size(), 10);
            assertEquals(repo1.size(), 10);
            assertEquals(repo2.size(), 10);
            assertEquals(repo3.size(), 10);

            Document document = createDocument("firstName", faker.name().firstName())
                .put("id", 12L);
            col1.insert(document);

            transaction.commit();

            fail();
        } catch (TransactionException e) {
            assert transaction != null;
            transaction.rollback();

            assertEquals(col1.size(), 11); // last doc added
            assertEquals(repo1.size(), 10);
            assertEquals(repo2.size(), 10);
            assertEquals(repo3.size(), 10);
        }
    }

    @Test(expected = TransactionException.class)
    public void testFailureOnClosedTransaction() {
        try(Session session = db.createSession()) {
            Transaction transaction = session.beginTransaction();
            ObjectRepository<TxData> txRepo = transaction.getRepository(TxData.class);
            txRepo.insert(new TxData(1L, "John"));
            transaction.commit();

            txRepo.insert(new TxData(2L, "Jane"));
            fail();
        }
    }
}
