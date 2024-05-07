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

package org.dizitart.no2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
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
import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.integration.TestUtil;
import org.dizitart.no2.integration.repository.data.EmptyClass;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;
import static org.dizitart.no2.common.Constants.META_MAP_NAME;
import static org.dizitart.no2.common.util.Iterables.listOf;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
public class NitriteTest {
    private Nitrite db;
    private NitriteCollection collection;
    private SimpleDateFormat simpleDateFormat;
    private final String fileName = getRandomTempDbFile();

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() throws ParseException {
        db = TestUtil.createDb(fileName, "test-user", "test-password");

        SimpleNitriteMapper documentMapper = (SimpleNitriteMapper) db.getConfig().nitriteMapper();
        documentMapper.registerEntityConverter(new CompatChild.Converter());
        documentMapper.registerEntityConverter(new Receipt.Converter());
        documentMapper.registerEntityConverter(new EmptyClass.Converter());

        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

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
        deleteDb(fileName);
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
    public void testCompact() {
        long initialSize = new File(fileName).length();
        db.commit();
        db.close();
        // according to documentation MVStore.compactMoveChunks() size would
        // increase temporarily
        assertTrue(new File(fileName).length() > initialSize);
    }

    @Test
    public void testReopen() throws ParseException {
        assertNotNull(db);
        NitriteCollection testCollection = db.getCollection("test");
        assertNotNull(testCollection);
        long prevSize = testCollection.find().size();
        ObjectRepository<Receipt> repository = db.getRepository(Receipt.class);
        assertNotNull(repository);
        long prevRepoSize = repository.size();

        db.close();
        db = null;

        db = TestUtil.createDb(fileName, "test-user", "test-password", listOf(new Receipt.Converter()));
        assertNotNull(db);
        testCollection = db.getCollection("test");
        assertNotNull(testCollection);
        long sizeNow = testCollection.find().size();
        assertEquals(prevSize, sizeNow);
        repository = db.getRepository(Receipt.class);
        assertNotNull(repository);
        long repoSizeNow = repository.size();
        assertEquals(prevRepoSize, repoSizeNow);

        db.close();
        db = null;
        db = TestUtil.createDb(fileName, "test-user", "test-password", listOf(new Receipt.Converter()));

        testCollection = db.getCollection("test");
        testCollection.insert(createDocument("firstName", "fn12")
            .put("lastName", "ln12")
            .put("birthDay", simpleDateFormat.parse("2010-07-01T16:02:48.440Z"))
            .put("data", new byte[]{10, 20, 30})
            .put("body", "a quick brown fox jump over the lazy dog"));
        repository = db.getRepository(Receipt.class);
        Receipt r = new Receipt();
        r.status = Receipt.Status.COMPLETED;
        r.clientRef = "10";
        r.synced = false;
        repository.insert(r);

        db.close();
        db = null;
        db = TestUtil.createDb(fileName, "test-user", "test-password", listOf(new Receipt.Converter()));

        testCollection = db.getCollection("test");
        assertNotNull(testCollection);
        sizeNow = testCollection.find().size();
        assertEquals(prevSize + 1, sizeNow);
        repository = db.getRepository(Receipt.class);
        assertNotNull(repository);
        repoSizeNow = repository.size();
        assertEquals(prevRepoSize + 1, repoSizeNow);
    }

    @Test
    public void testClose() {
        NitriteCollection testCollection = db.getCollection("test");
        testCollection.insert(createDocument("a", "b"));
        db.close();

        assertFalse(testCollection.isOpen());
    }

    @Test(expected = NitriteIOException.class)
    public void testCloseReadonlyDatabase() {
        db.close();
        db = null;

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(fileName)
            .compress(true)
            .readOnly(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate("test-user", "test-password");
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
        db.getRepository((Class<? extends Object>) null);
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

    @Test(expected = NitriteIOException.class)
    public void testIssue112() {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(System.getProperty("java.io.tmpdir"))
            .compress(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();
        assertNull(db);
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
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                    repository.remove(receipt);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                } catch (Throwable t) {
                    log.error("Error in thread", t);
                }
            }
            latch.countDown();
        }).start();

        for (int i = 0; i < 1000; ++i) {
            repository.find(where("status").eq(Receipt.Status.COMPLETED).not(),
                FindOptions.orderBy("createdTimestamp", SortOrder.Descending)).toList();
            try {
                Thread.sleep(50);
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
                int refIndex = random.nextInt(5);
                Receipt receipt = factory.manufacturePojoWithFullData(Receipt.class);
                receipt.setClientRef(refs[refIndex]);
                repository.update(receipt, true);
                latch.countDown();
            });
        }

        latch.await();
        assertTrue(repository.find().size() <= 5);
        pool.shutdown();
    }

    @Test
    public void testReadCompatibility() throws IOException {
//      ******* Old DB Creation Code Start *********
//
//        Nitrite db = new NitriteBuilder()
//            .filePath("/tmp/no2-old.db")
//            .compressed()
//            .openOrCreate("test-user", "test-password");
//
//        NitriteCollection collection = db.getCollection("test");
//        Document doc = createDocument("first_key", 1)
//            .put("second_key", "quick brown fox")
//            .put("third_key", 0.5);
//        collection.insert(doc);
//
//        Document doc2 = createDocument("first_key", 10)
//            .put("second_key", "jump over lazy dog")
//            .put("third_key", 0.25);
//        collection.insert(doc2);
//
//        collection.createIndex("first_key", IndexOptions.indexOptions(IndexType.Unique));
//        collection.createIndex("second_key", IndexOptions.indexOptions(IndexType.Fulltext));
//
//        List<Document> cursor = collection.find(Filters.and(Filters.eq("first_key", 1),
//            Filters.text("second_key", "fox"))).toList();
//        assertEquals(cursor.size(), 1);
//        assertEquals(cursor.get(0).get("third_key"), 0.5);
//
//        ObjectRepository<Receipt> repository = db.getRepository(Receipt.class);
//        ObjectRepository<Receipt> orangeRepository = db.getRepository("orange", Receipt.class);
//
//        Receipt r1 = new Receipt();
//        r1.status = Receipt.Status.PREPARING;
//        r1.clientRef = "1";
//        r1.synced = true;
//
//        Receipt r2 = new Receipt();
//        r2.status = Receipt.Status.COMPLETED;
//        r2.clientRef = "10";
//        r2.synced = false;
//
//        repository.insert(r1, r2);
//        orangeRepository.insert(r1, r2);
//
//        assertTrue(repository.hasIndex("synced"));
//
//        List<Receipt> list = repository.find(ObjectFilters.and(ObjectFilters.eq("synced", true),
//            ObjectFilters.eq("status", Receipt.Status.PREPARING))).toList();
//        assertEquals(list.size(), 1);
//        assertEquals(list.get(0).clientRef, "1");
//
//        list = orangeRepository.find(ObjectFilters.and(ObjectFilters.eq("synced", false),
//            ObjectFilters.eq("status", Receipt.Status.PREPARING))).toList();
//        assertEquals(list.size(), 0);
//
//      ******* Old DB Creation Code End *********

        if (Files.exists(Paths.get(System.getProperty("java.io.tmpdir") + File.separator + "old.db"))) {
            Files.delete(Paths.get(System.getProperty("java.io.tmpdir") + File.separator + "old.db"));
        }

        InputStream stream = ClassLoader.getSystemResourceAsStream("no2-old.db");
        if (stream == null) {
            stream = ClassLoader.getSystemClassLoader().getResourceAsStream("no2-old.db");
        }
        assert stream != null;

        Files.copy(stream, Paths.get(System.getProperty("java.io.tmpdir") + File.separator + "old.db"));

        String oldDbFile = System.getProperty("java.io.tmpdir") + File.separator + "old.db";
        Nitrite db = TestUtil.createDb(oldDbFile, "test-user", "test-password", listOf(new Receipt.Converter()));

        NitriteCollection collection = db.getCollection("test");

        // text filter has been the first filter in and clause
        List<Document> cursor = collection.find(
            and(where("second_key").text("fox"), where("first_key").eq(1))).toList();
        assertEquals(cursor.size(), 1);
        assertEquals(cursor.get(0).get("third_key"), 0.5);

        ObjectRepository<Receipt> repository = db.getRepository(Receipt.class);
        ObjectRepository<Receipt> orangeRepository = db.getRepository(Receipt.class, "orange");

        List<Receipt> list = repository.find(
            and(where("synced").eq(true), where("status").eq(Receipt.Status.PREPARING.toString()))).toList();
        assertEquals(list.size(), 1);
        assertEquals(list.get(0).clientRef, "1");

        list = orangeRepository.find(
            and(where("synced").eq(false), where("status").eq(Receipt.Status.PREPARING.toString()))).toList();
        assertEquals(list.size(), 0);
        assertNotNull(repository.getAttributes());

        db.close();

        //TODO: CHeck reopen with repo. That should also fails too.
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
                    log.error("Error in thread", e);
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
        collection.close();
        db.close();
    }

    @Test
    public void testReadOnlyMode() {
        NitriteCollection nitriteCollection = db.getCollection("readonly-test");
        nitriteCollection.insert(createDocument("a", "b"));
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "a");

        ObjectRepository<Receipt> repository = db.getRepository(Receipt.class);
        Receipt receipt = new Receipt();
        receipt.clientRef = "111-11111";
        receipt.status = Receipt.Status.PREPARING;
        repository.insert(receipt);

        db.close();

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(fileName)
            .readOnly(true)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .registerEntityConverter(new Receipt.Converter())
            .openOrCreate("test-user", "test-password");

        assertFalse(db.hasUnsavedChanges());

        nitriteCollection = db.getCollection("readonly-test");
        assertEquals(nitriteCollection.find().size(), 1);
        assertTrue(nitriteCollection.hasIndex("a"));

        repository = db.getRepository(Receipt.class);
        assertEquals(repository.find().size(), 1);
        assertTrue(repository.hasIndex("synced"));

        assertFalse(db.hasUnsavedChanges());
        db.close();
        deleteDb(fileName);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompatChild {
        private Long childId;
        private String lastName;

        public static class Converter implements EntityConverter<CompatChild> {

            @Override
            public Class<CompatChild> getEntityType() {
                return CompatChild.class;
            }

            @Override
            public Document toDocument(CompatChild entity, NitriteMapper nitriteMapper) {
                return Document.createDocument("childId", entity.childId)
                    .put("lastName", entity.lastName);
            }

            @Override
            public CompatChild fromDocument(Document document, NitriteMapper nitriteMapper) {
                CompatChild entity = new CompatChild();
                entity.childId = document.get("childId", Long.class);
                entity.lastName = document.get("lastName", String.class);
                return entity;
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Index(fields = "synced", type = IndexType.NON_UNIQUE)
    public static class Receipt {
        @Id
        private String clientRef;
        private Boolean synced;
        private Status status;
        private Long createdTimestamp = System.currentTimeMillis();

        public static class Converter implements EntityConverter<Receipt> {

            @Override
            public Class<Receipt> getEntityType() {
                return Receipt.class;
            }

            @Override
            public Document toDocument(Receipt entity, NitriteMapper nitriteMapper) {
                return createDocument("status", entity.status)
                    .put("clientRef", entity.clientRef)
                    .put("synced", entity.synced)
                    .put("createdTimestamp", entity.createdTimestamp);
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

        public enum Status {
            COMPLETED,
            PREPARING,
        }
    }
}
