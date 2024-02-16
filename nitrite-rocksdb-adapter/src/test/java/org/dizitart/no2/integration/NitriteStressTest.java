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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.SimpleNitriteMapper;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.Indices;
import org.junit.*;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class NitriteStressTest {
    private static final int TEST_SET_COUNT = 15000;
    private final PodamFactory podamFactory = new PodamFactoryImpl();
    private final String fileName = getRandomTempDbFile();
    private Nitrite db;
    private NitriteCollection collection;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void before() {
        db = createDb(fileName);
        SimpleNitriteMapper documentMapper = (SimpleNitriteMapper) db.getConfig().nitriteMapper();
        documentMapper.registerEntityConverter(new TestDto.Converter());
        documentMapper.registerEntityConverter(new PerfTest.Converter());
        documentMapper.registerEntityConverter(new PerfTestIndexed.Converter());

        collection = db.getCollection("test");
    }

    @After
    public void cleanUp() {
        if (db != null && !db.isClosed()) {
            long start = System.currentTimeMillis();
            db.close();
        }

        deleteDb(fileName);
    }

    @Test
    public void stressTest() {
        ObjectRepository<TestDto> testRepository = db.getRepository(TestDto.class);
        testRepository.createIndex(IndexOptions.indexOptions(IndexType.FULL_TEXT), "lastName");
        testRepository.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "birthDate");

        int counter = 0;
        try {
            for (TestDto testDto : createTestSet()) {
                testRepository.insert(testDto);
                counter++;
            }
        } catch (Throwable t) {
            log.error("Error inserting record", t);
            throw t;
        }

        int size = testRepository.find().toList().size();
        assertEquals(counter, size);
    }

    @Test
    @Ignore
    public void testIssue902() {
        NitriteCollection nitriteCollection = db.getCollection("testIssue902");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "name");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "age");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "address");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "email");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "phone");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "dateOfBirth");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "company");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "balance");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "isActive");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "guid");
        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "index");

        for (int i = 0; i < 10; i++) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            for (int j = 0; j < 3000; j++) {
                Document document = Document.createDocument();
                document.put("name", "name" + j);
                document.put("age", j);
                document.put("address", "address" + j);
                document.put("email", "email" + j);
                document.put("phone", "phone" + j);
                document.put("dateOfBirth", "dateOfBirth" + j);
                document.put("company", "company" + j);
                document.put("balance", j);
                document.put("isActive", true);
                document.put("guid", UUID.randomUUID().toString());
                document.put("index", j);
                nitriteCollection.insert(document);
            }
            stopWatch.stop();
            log.error("Time taken to insert 30000 records: " + stopWatch.getTime());
        }

        if (db.hasUnsavedChanges()) {
            db.commit();
        }

        Document updatedDocument = Document.createDocument("unrelatedField", "unrelatedValue");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        nitriteCollection.update(where("balance").eq(200000), updatedDocument, updateOptions(false));
        stopWatch.stop();

        log.error("Time taken to update 1 record: " + stopWatch.getTime());
    }

    @Test
    public void testIssue41() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "number");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "name");
        collection.createIndex("counter");

        Random random = new Random();
        AtomicLong counter = new AtomicLong(System.currentTimeMillis());
        PodamFactory factory = new PodamFactoryImpl();

        for (int i = 0; i < 100000; i++) {
            Document doc = Document.createDocument();
            doc.put("number", random.nextDouble());
            doc.put("name", factory.manufacturePojo(String.class));
            doc.put("counter", counter.getAndIncrement());
            collection.insert(doc);
        }

        if (db.hasUnsavedChanges()) {
            db.commit();
        }

        DocumentCursor cursor = collection.find();
        for (Document element : cursor) {
            assertNotNull(element);
        }
    }

    @Test
    public void testRepoPerformanceWithIndex() {
        // warm-up
        List<PerfTestIndexed> items = getItems(PerfTestIndexed.class);
        ObjectRepository<PerfTestIndexed> repo = db.getRepository(PerfTestIndexed.class);
        for (PerfTestIndexed item : items) {
            assertNotNull(item);
            repo.insert(item);
        }
        repo.remove(Filter.ALL);
        repo.drop();

        // actual calculation
        repo = db.getRepository(PerfTestIndexed.class);
        for (PerfTestIndexed item : items) {
            repo.insert(item);
        }

        repo.remove(Filter.ALL);
    }

    @Test
    public void testRepoPerformanceWithoutIndex() {
        // warm-up
        List<PerfTest> items = getItems(PerfTest.class);
        ObjectRepository<PerfTest> repo = db.getRepository(PerfTest.class);
        for (PerfTest item : items) {
            assertNotNull(item);
            repo.insert(item);
        }
        repo.remove(Filter.ALL);
        repo.drop();

        // actual calculation
        repo = db.getRepository(PerfTest.class);
        for (PerfTest item : items) {
            repo.insert(item);
        }

        repo.remove(Filter.ALL);
    }

    private List<TestDto> createTestSet() {
        List<TestDto> testData = new ArrayList<>();
        for (int i = 0; i < TEST_SET_COUNT; i++) {
            TestDto testRecords = podamFactory.manufacturePojo(TestDto.class);
            testData.add(testRecords);
        }
        return testData;
    }

    private <T> List<T> getItems(Class<T> type) {
        PodamFactory generator = new PodamFactoryImpl();
        List<T> items = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            items.add(generator.manufacturePojoWithFullData(type));
        }
        assertEquals(items.size(), 10000);
        return items;
    }

    @Data
    public static class TestDto {

        @XmlElement(
            name = "StudentNumber",
            required = true
        )
        @Id
        protected String studentNumber;

        @XmlElement(
            name = "LastName",
            required = true
        )
        protected String lastName;

        @XmlElement(
            name = "Prefixes"
        )
        protected String prefixes;

        @XmlElement(
            name = "Initials",
            required = true
        )
        protected String initials;

        @XmlElement(
            name = "FirstNames"
        )
        protected String firstNames;
        @XmlElement(
            name = "Nickname"
        )
        protected String nickName;

        @XmlElement(
            name = "BirthDate",
            required = true
        )
        @XmlSchemaType(
            name = "date"
        )
        protected String birthDate;


        public TestDto() {
        }

        public static class Converter implements EntityConverter<TestDto> {

            @Override
            public Class<TestDto> getEntityType() {
                return TestDto.class;
            }

            @Override
            public Document toDocument(TestDto entity, NitriteMapper nitriteMapper) {
                return Document.createDocument()
                    .put("studentNumber", entity.studentNumber)
                    .put("lastName", entity.lastName)
                    .put("prefixes", entity.prefixes)
                    .put("initials", entity.initials)
                    .put("firstNames", entity.firstNames)
                    .put("nickName", entity.nickName)
                    .put("birthDate", entity.birthDate);
            }

            @Override
            public TestDto fromDocument(Document document, NitriteMapper nitriteMapper) {
                TestDto entity = new TestDto();
                entity.studentNumber = document.get("studentNumber", String.class);
                entity.lastName = document.get("lastName", String.class);
                entity.prefixes = document.get("prefixes", String.class);
                entity.initials = document.get("initials", String.class);
                entity.firstNames = document.get("firstNames", String.class);
                entity.nickName = document.get("nickName", String.class);
                entity.birthDate = document.get("birthDate", String.class);
                return entity;
            }
        }
    }

    @Data
    public static class PerfTest {
        private String firstName;
        private String lastName;
        private Integer age;
        private String text;

        public static class Converter implements EntityConverter<PerfTest> {

            @Override
            public Class<PerfTest> getEntityType() {
                return PerfTest.class;
            }

            @Override
            public Document toDocument(PerfTest entity, NitriteMapper nitriteMapper) {
                Document document = Document.createDocument();
                document.put("firstName", entity.firstName);
                document.put("lastName", entity.lastName);
                document.put("age", entity.age);
                document.put("text", entity.text);
                return document;
            }

            @Override
            public PerfTest fromDocument(Document document, NitriteMapper nitriteMapper) {
                PerfTest entity = new PerfTest();
                entity.firstName = (String) document.get("firstName");
                entity.lastName = (String) document.get("lastName");
                entity.age = (Integer) document.get("age");
                entity.text = (String) document.get("text");
                return entity;
            }
        }
    }

    @Indices({
        @Index(fields = "firstName", type = IndexType.NON_UNIQUE),
        @Index(fields = "age", type = IndexType.NON_UNIQUE),
        @Index(fields = "text", type = IndexType.FULL_TEXT),
    })
    private static class PerfTestIndexed extends PerfTest {
        public static class Converter implements EntityConverter<PerfTestIndexed> {

            @Override
            public Class<PerfTestIndexed> getEntityType() {
                return PerfTestIndexed.class;
            }

            @Override
            public Document toDocument(PerfTestIndexed entity, NitriteMapper nitriteMapper) {
                Document document = Document.createDocument();
                document.put("firstName", entity.getFirstName());
                document.put("lastName", entity.getLastName());
                document.put("age", entity.getAge());
                document.put("text", entity.getText());
                return document;
            }

            @Override
            public PerfTestIndexed fromDocument(Document document, NitriteMapper nitriteMapper) {
                PerfTestIndexed entity = new PerfTestIndexed();
                entity.setFirstName((String) document.get("firstName"));
                entity.setLastName((String) document.get("lastName"));
                entity.setAge((Integer) document.get("age"));
                entity.setText((String) document.get("text"));
                return entity;
            }
        }
    }
}
