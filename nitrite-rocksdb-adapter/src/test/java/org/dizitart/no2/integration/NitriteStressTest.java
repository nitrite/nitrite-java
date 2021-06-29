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
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.mapper.Mappable;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.dizitart.no2.integration.TestUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author Anindya Chatterjee
 */
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
        collection = db.getCollection("test");
        System.out.println(fileName);
    }

    @After
    public void cleanUp() {
        if (db != null && !db.isClosed()) {
            long start = System.currentTimeMillis();
            db.close();
            System.out.println("Time to compact and close - " + (System.currentTimeMillis() - start) / 1000 + " seconds");
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
            System.err.println("Crashed after " + counter + " records");
            throw t;
        }

        int size = testRepository.find().toList().size();
        assertEquals(counter, size);
    }

    @Test
    public void testIssue41() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "number");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "name");
        collection.createIndex("counter");

        Random random = new Random();
        AtomicLong counter = new AtomicLong(System.currentTimeMillis());
        PodamFactory factory = new PodamFactoryImpl();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            Document doc = Document.createDocument();
            doc.put("number", random.nextDouble());
            doc.put("name", factory.manufacturePojo(String.class));
            doc.put("counter", counter.getAndIncrement());
            collection.insert(doc);
            if (i % 10000 == 0) {
                System.out.println(i + " entries written");
            }
        }
        System.out.println("Records inserted in " + ((System.currentTimeMillis() - start) / (1000 * 60)) + " minutes");

        if (db.hasUnsavedChanges()) {
            db.commit();
        }

        start = System.currentTimeMillis();
        DocumentCursor cursor = collection.find();
        System.out.println("Size ->" + cursor.size());
        System.out.println("Records size calculated in " + ((System.currentTimeMillis() - start) / (1000)) + " seconds");

        int i = 0;
        start = System.currentTimeMillis();
        for (Document element : cursor) {
            assertNotNull(element);
            i++;
            if (i % 10000 == 0) {
                System.out.println(i + " entries processed");
            }
        }
        System.out.println("Iteration completed in " + ((System.currentTimeMillis() - start) / (1000)) + " seconds");
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
        long start = System.currentTimeMillis();
        for (PerfTestIndexed item : items) {
            repo.insert(item);
        }
        long diff = System.currentTimeMillis() - start;
        System.out.println("Time take to insert 10000 indexed items - " + diff + "ms");

        start = System.currentTimeMillis();
        repo.remove(Filter.ALL);
        diff = System.currentTimeMillis() - start;
        System.out.println("Time take to remove 10000 indexed items - " + diff + "ms");
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
        long start = System.currentTimeMillis();
        for (PerfTest item : items) {
            repo.insert(item);
        }
        long diff = System.currentTimeMillis() - start;
        System.out.println("Time take to insert 10000 non-indexed items - " + diff + "ms");

        start = System.currentTimeMillis();
        repo.remove(Filter.ALL);
        diff = System.currentTimeMillis() - start;
        System.out.println("Time take to remove 10000 non-indexed items - " + diff + "ms");
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
    public static class TestDto implements Mappable {

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

        @Override
        public Document write(NitriteMapper mapper) {
            return Document.createDocument()
                .put("studentNumber", studentNumber)
                .put("lastName", lastName)
                .put("prefixes", prefixes)
                .put("initials", initials)
                .put("firstNames", firstNames)
                .put("nickName", nickName)
                .put("birthDate", birthDate);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            studentNumber = document.get("studentNumber", String.class);
            lastName = document.get("lastName", String.class);
            prefixes = document.get("prefixes", String.class);
            initials = document.get("initials", String.class);
            firstNames = document.get("firstNames", String.class);
            nickName = document.get("nickName", String.class);
            birthDate = document.get("birthDate", String.class);
        }
    }

    @Data
    public static class PerfTest implements Mappable {
        private String firstName;
        private String lastName;
        private Integer age;
        private String text;

        @Override
        public Document write(NitriteMapper mapper) {
            Document document = Document.createDocument();
            document.put("firstName", firstName);
            document.put("lastName", lastName);
            document.put("age", age);
            document.put("text", text);
            return document;
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            this.firstName = (String) document.get("firstName");
            this.lastName = (String) document.get("lastName");
            this.age = (Integer) document.get("age");
            this.text = (String) document.get("text");
        }
    }

    @Indices({
        @Index(value = "firstName", type = IndexType.NON_UNIQUE),
        @Index(value = "age", type = IndexType.NON_UNIQUE),
        @Index(value = "text", type = IndexType.FULL_TEXT),
    })
    private static class PerfTestIndexed extends PerfTest {
    }
}
