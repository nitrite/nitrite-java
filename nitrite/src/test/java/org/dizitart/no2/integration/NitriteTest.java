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
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.common.mapper.Mappable;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.Indices;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;
import static org.dizitart.no2.common.Constants.META_MAP_NAME;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.FluentFilter.where;
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
        db = createDb("test-user", "test-password");

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
    public void tearDown() throws Exception {
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

    @Test
    public void testListRepositories() {
        db.getRepository(getClass());
        Set<String> repositories = db.listRepositories();
        assertEquals(repositories.size(), 1);
    }

    @Test
    public void testHasCollection() {
        assertTrue(db.hasCollection("test"));
        assertFalse(db.hasCollection("lucene" + INTERNAL_NAME_SEPARATOR + "test"));
    }

    @Test
    public void testHasRepository() {
        db.getRepository(getClass());
        assertTrue(db.hasRepository(getClass()));
        assertFalse(db.hasRepository(String.class));
    }

    @Test
    public void testClose() throws Exception {
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

    @Test
    public void testGetRepository() {
        ObjectRepository<NitriteTest> repository = db.getRepository(NitriteTest.class);
        assertNotNull(repository);
        assertEquals(repository.getType(), NitriteTest.class);
    }

    @Test
    public void testGetRepositoryWithKey() {
        ObjectRepository<NitriteTest> repository = db.getRepository(NitriteTest.class, "key");
        assertNotNull(repository);
        assertEquals(repository.getType(), NitriteTest.class);
        assertFalse(db.hasRepository(NitriteTest.class));
        assertTrue(db.hasRepository(NitriteTest.class, "key"));
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
        ObjectRepository<NitriteTest> repository = db.getRepository(NitriteTest.class);
        assertNotNull(repository);
        assertEquals(repository.getType(), NitriteTest.class);

        ObjectRepository<NitriteTest> repository2 = db.getRepository(NitriteTest.class);
        assertNotNull(repository2);
        assertEquals(repository2.getType(), NitriteTest.class);
    }

    @Test(expected = ValidationException.class)
    public void testGetRepositoryInvalid() {
        db.getRepository(null);
    }

    @Test(expected = NitriteIOException.class)
    public void testGetCollectionNullStore() throws Exception {
        db = Nitrite.builder().openOrCreate();
        db.close();
        db.getCollection("test");
    }

    @Test(expected = NitriteIOException.class)
    public void testGetRepositoryNullStore() throws Exception {
        db = Nitrite.builder().openOrCreate();
        db.close();
        db.getRepository(NitriteTest.class);
    }

    @Test(expected = NitriteIOException.class)
    public void testGetKeyedRepositoryNullStore() throws Exception {
        db = Nitrite.builder().openOrCreate();
        db.close();
        db.getRepository(NitriteTest.class, "key");
    }

    @Test(expected = NitriteIOException.class)
    public void testCommitNullStore() throws Exception {
        db = Nitrite.builder().openOrCreate();
        db.close();
        db.commit();
    }

    @Test(expected = ValidationException.class)
    public void testGetCollectionInvalidName() {
        db.getCollection(META_MAP_NAME);
    }

    @Test
    public void testIssue185() throws InterruptedException {
        final ObjectRepository<Receipt> repository = db.getRepository(Receipt.class);
        final Receipt receipt = new Receipt();
        receipt.clientRef = "111-11111";
        receipt.status = Receipt.Status.PREPARING;
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            for (int i = 0; i < 1000; ++i) {
                try {
                    repository.update(receipt, true);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ignored) {
                    }
                    repository.remove(receipt);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ignored) {
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            latch.countDown();
        }).start();

        for (int i = 0; i < 1000; ++i) {
            repository.find(where("status").eq(Receipt.Status.COMPLETED).not(),
                FindOptions.orderBy("createdTimestamp", SortOrder.Descending)).toList();
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {
            }
        }
        latch.await();
    }

    @Test
    public void testIssue193() throws InterruptedException {
        final ObjectRepository<Receipt> repository = db.getRepository(Receipt.class);
        final PodamFactory factory = new PodamFactoryImpl();
        final String[] refs = new String[]{"1", "2", "3", "4", "5"};
        final Random random = new Random();
        ExecutorService pool = ThreadPoolManager.workerPool();

        final CountDownLatch latch = new CountDownLatch(10000);
        for (int i = 0; i < 10000; i++) {
            pool.submit(() -> {
                try {
                    int refIndex = random.nextInt(5);
                    Receipt receipt = factory.manufacturePojoWithFullData(Receipt.class);
                    receipt.setClientRef(refs[refIndex]);
                    repository.update(receipt, true);
                    latch.countDown();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
        }

        latch.await();
        assertTrue(repository.find().size() <= 5);
        pool.shutdown();
    }

    @Test
    public void testIssue212() {
        NitriteCollection collection = db.getCollection("testIssue212");
        Document doc1 = createDocument("key", "key").put("second_key", "second_key").put("third_key", "third_key");
        Document doc2 = createDocument("key", "key").put("second_key", "second_key").put("fourth_key", "fourth_key");
        Document doc = createDocument("fifth_key", "fifth_key");

        if (!collection.hasIndex("key")) {
            collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "key");
        }
        if (!collection.hasIndex("second_key")) {
            collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "second_key");
        }

        collection.insert(doc1, doc2);
        collection.update(where("key").eq("key").and(where("second_key").eq("second_key")),
            doc, UpdateOptions.updateOptions(true));

        for (Document document : collection.find()) {
            System.out.println(document);
        }
    }

    @Test
    public void testIssue245() throws Exception {
        class ThreadRunner implements Runnable {
            @Override
            public void run() {
                try {
                    long id = Thread.currentThread().getId();
                    NitriteCollection collection = db.getCollection("testIssue245");

                    for (int i = 0; i < 5; i++) {

                        System.out.println("Thread ID = " + id + " Inserting doc " + i);
                        Document doc = Document.createDocument(UUID.randomUUID().toString(), UUID.randomUUID().toString());

                        WriteResult result = collection.insert(doc);//db.commit();
                        System.out.println("Result of insert = " + result.getAffectedCount());
                        System.out.println("Thread id = " + id + " --> count = " + collection.size());

                        Thread.sleep(10);

                    }//for closing

                    collection.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t0 = new Thread(new ThreadRunner());
        Thread t1 = new Thread(new ThreadRunner());
        Thread t2 = new Thread(new ThreadRunner());

        t0.start();
        t1.start();
        t2.start();

        Thread.sleep(10 * 1000);

        t0.join();
        t1.join();
        t2.join();

        NitriteCollection collection = db.getCollection("testIssue245");
        System.out.println("No of Documents = " + collection.size());
        collection.close();
        db.close();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompatChild implements Mappable {
        private Long childId;
        private String lastName;

        @Override
        public Document write(NitriteMapper mapper) {
            return Document.createDocument("childId", childId)
                .put("lastName", lastName);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            childId = document.get("childId", Long.class);
            lastName = document.get("lastName", String.class);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Indices({
        @Index(value = "synced", type = IndexType.NON_UNIQUE)
    })
    public static class Receipt implements Mappable {
        @Id
        private String clientRef;
        private Boolean synced;
        private Long createdTimestamp = System.currentTimeMillis();
        private Status status;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument("status", status)
                .put("clientRef", clientRef)
                .put("synced", synced)
                .put("createdTimestamp", createdTimestamp);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            if (document != null) {
                Object status = document.get("status");
                if (status instanceof Status) {
                    this.status = (Status) status;
                } else {
                    this.status = Status.valueOf(status.toString());
                }
                this.clientRef = document.get("clientRef", String.class);
                this.synced = document.get("synced", Boolean.class);
                this.createdTimestamp = document.get("createdTimestamp", Long.class);
            }
        }

        public enum Status {
            COMPLETED,
            PREPARING,
        }
    }
}
