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
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.SimpleNitriteMapper;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.integration.repository.data.EmptyClass;
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

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.collection.FindOptions.orderBy;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;
import static org.dizitart.no2.common.Constants.META_MAP_NAME;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
public class NitriteTest {
    @Rule
    public Retry retry = new Retry(3);
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() throws ParseException {
        db = createDb("test-user", "test-password");
        SimpleNitriteMapper nitriteMapper = (SimpleNitriteMapper) db.getConfig().nitriteMapper();
        nitriteMapper.registerEntityConverter(new Receipt.ReceiptConverter());
        nitriteMapper.registerEntityConverter(new CompatChild.CompatChildConverter());
        nitriteMapper.registerEntityConverter(new EmptyClass.Converter());

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
        db.getRepository(getClass());
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
        db.getRepository(getClass());
    }

    @Test
    public void testHasRepository2() {
        db.getRepository(Receipt.class);
        assertTrue(db.hasRepository(Receipt.class));
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
        db.getRepository(EmptyClass.class);
    }

    @Test(expected = NitriteIOException.class)
    public void testGetKeyedRepositoryNullStore() {
        db = Nitrite.builder().openOrCreate();
        db.close();
        db.getRepository(EmptyClass.class, "key");
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
                    log.error("Error while updating receipt", t);
                }
            }
            latch.countDown();
        }).start();

        for (int i = 0; i < 1000; ++i) {
            repository.find(where("status").eq(Receipt.Status.COMPLETED).not(),
                orderBy("createdTimestamp", SortOrder.Descending)).toList();
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
                    log.error("Error while updating receipt", t);
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
            assertNotNull(document);
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
                        Document doc = Document.createDocument(UUID.randomUUID().toString(), UUID.randomUUID().toString());

                        collection.insert(doc);//db.commit();
                        Thread.sleep(10);

                    }//for closing

                    collection.close();

                } catch (Exception e) {
                    log.error("Error while inserting document", e);
                }
            }
        }

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
        collection.close();
        db.close();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompatChild {
        private Long childId;
        private String lastName;

        public static class CompatChildConverter implements EntityConverter<CompatChild> {

            @Override
            public Class<CompatChild> getEntityType() {
                return CompatChild.class;
            }

            @Override
            public Document toDocument(CompatChild entity, NitriteMapper nitriteMapper) {
                return Document.createDocument("childId", entity.getChildId())
                    .put("lastName", entity.getLastName());
            }

            @Override
            public CompatChild fromDocument(Document document, NitriteMapper nitriteMapper) {
                CompatChild compatChild = new CompatChild();
                compatChild.setChildId(document.get("childId", Long.class));
                compatChild.setLastName(document.get("lastName", String.class));
                return compatChild;
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Indices({
        @Index(fields = "synced", type = IndexType.NON_UNIQUE)
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

        public static class ReceiptConverter implements EntityConverter<Receipt> {

            @Override
            public Class<Receipt> getEntityType() {
                return Receipt.class;
            }

            @Override
            public Document toDocument(Receipt entity, NitriteMapper nitriteMapper) {
                return createDocument("status", entity.getStatus())
                    .put("clientRef", entity.getClientRef())
                    .put("synced", entity.getSynced())
                    .put("createdTimestamp", entity.getCreatedTimestamp());
            }

            @Override
            public Receipt fromDocument(Document document, NitriteMapper nitriteMapper) {
                Receipt receipt = new Receipt();

                if (document != null) {
                    Object status = document.get("status");
                    if (status != null) {
                        if (status instanceof Status) {
                            receipt.status = (Status) status;
                        } else {
                            receipt.status = Status.valueOf(status.toString());
                        }
                    }
                    receipt.clientRef = document.get("clientRef", String.class);
                    receipt.synced = document.get("synced", Boolean.class);
                    receipt.createdTimestamp = document.get("createdTimestamp", Long.class);
                }
                return receipt;
            }
        }
    }
}
