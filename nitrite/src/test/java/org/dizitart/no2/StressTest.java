/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2;

import lombok.Data;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
@Ignore
public class StressTest {
    private String fileName = getRandomTempDbFile();
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void before() {
        db = Nitrite
                .builder()
                .compressed()
                .filePath(fileName)
                .openOrCreate();
        collection = db.getCollection("test");
        System.out.println(fileName);
    }

    @Test
    public void testIssue41() {
        collection.createIndex("number", IndexOptions.indexOptions(IndexType.NonUnique));
        collection.createIndex("name", IndexOptions.indexOptions(IndexType.NonUnique));
        collection.createIndex("counter", IndexOptions.indexOptions(IndexType.Unique));

        Random random = new Random();
        AtomicLong counter = new AtomicLong(System.currentTimeMillis());
        PodamFactory factory = new PodamFactoryImpl();

        long start= System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            Document doc = new Document();
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

        start= System.currentTimeMillis();
        Cursor cursor = collection.find();
        System.out.println("Size ->" + cursor.size());
        System.out.println("Records size calculated in " + ((System.currentTimeMillis() - start) / (1000)) + " seconds");

        int i = 0;
        for (Document element : cursor) {
            assertNotNull(element);
            i++;
            if (i % 10000 == 0) {
                System.out.println(i + " entries processed");
            }
        }
    }

    @After
    public void clear() throws IOException {
        if (db != null && !db.isClosed()) {
            long start = System.currentTimeMillis();
            db.close();
            System.out.println("Time to compact and close - " + (System.currentTimeMillis() - start) / 1000 + " seconds");
        }
        Files.delete(Paths.get(fileName));
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
        repo.remove(ObjectFilters.ALL);
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
        repo.remove(ObjectFilters.ALL);
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
        repo.remove(ObjectFilters.ALL);
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
        repo.remove(ObjectFilters.ALL);
        diff = System.currentTimeMillis() - start;
        System.out.println("Time take to remove 10000 non-indexed items - " + diff + "ms");
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
    public static class PerfTest implements Mappable {
        private String firstName;
        private String lastName;
        private Integer age;
        private String text;

        @Override
        public Document write(NitriteMapper mapper) {
            Document document = new Document();
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
            @Index(value = "firstName", type = IndexType.NonUnique),
            @Index(value = "age", type = IndexType.NonUnique),
            @Index(value = "text", type = IndexType.Fulltext),
    })
    private static class PerfTestIndexed extends PerfTest {
    }
}
