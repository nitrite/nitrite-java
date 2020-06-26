/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.MVStoreConfig;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.StoreConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.module.NitriteModule.module;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteBuilderTest {
    private String fakeFile;
    private String filePath;

    @Before
    public void startup() {
        fakeFile = getRandomTempDbFile();
        filePath = getRandomTempDbFile();
    }

    @After
    public void cleanup() throws IOException {
        if (Files.exists(Paths.get(filePath))) {
            Files.delete(Paths.get(filePath));
        }

        if (Files.exists(Paths.get(fakeFile))) {
            Files.delete(Paths.get(fakeFile));
        }
    }

    @Test
    public void testConfig() throws IOException {
        NitriteBuilder builder = NitriteBuilder.get();
        builder.autoCommitBufferSize(1);
        builder.compressed();
        builder.disableAutoCommit();
        builder.disableAutoCompact();
        builder.filePath(filePath);
        builder.loadModule(module(new CustomIndexer()));

        Nitrite db = builder.openOrCreate();
        NitriteConfig config = builder.getNitriteConfig();
        MVStoreConfig storeConfig = (MVStoreConfig) config.getStoreConfig();

        assertEquals(storeConfig.getAutoCommitBufferSize(), 1);
        assertEquals(config.findIndexer("Custom").getClass(), CustomIndexer.class);
        assertFalse(storeConfig.isAutoCommit());
        assertFalse(storeConfig.isAutoCompact());
        assertTrue(storeConfig.isCompressed());
        assertFalse(storeConfig.isReadOnly());
        assertFalse(storeConfig.isInMemory());
        assertFalse(isNullOrEmpty(storeConfig.getFilePath()));

        db.close();

        builder = NitriteBuilder.get()
            .readOnly()
            .filePath(filePath);

        db = builder.openOrCreate();
        storeConfig = (MVStoreConfig) builder.getNitriteConfig().getStoreConfig();
        assertTrue(storeConfig.isReadOnly());
        db.close();

        Files.delete(Paths.get(filePath));
    }

    @Test
    public void testConfigWithFile() {
        File file = new File(filePath);
        NitriteBuilder builder = NitriteBuilder.get();
        builder.filePath(file);
        Nitrite db = builder.openOrCreate();
        StoreConfig storeConfig = builder.getNitriteConfig().getStoreConfig();

        assertFalse(storeConfig.isInMemory());
        assertFalse(isNullOrEmpty(storeConfig.getFilePath()));

        NitriteCollection test = db.getCollection("test");
        assertNotNull(test);

        db.commit();
        db.close();

        assertTrue(file.delete());
    }

    @Test
    public void testConfigWithFileNull() {
        File file = null;
        NitriteBuilder builder = NitriteBuilder.get();
        builder.filePath(file);
        Nitrite db = builder.openOrCreate();
        StoreConfig storeConfig = builder.getNitriteConfig().getStoreConfig();

        assertTrue(storeConfig.isInMemory());
        assertTrue(isNullOrEmpty(storeConfig.getFilePath()));

        NitriteCollection test = db.getCollection("test");
        assertNotNull(test);

        db.commit();
        db.close();
    }

    @Test
    public void testPopulateRepositories() {
        File file = new File(filePath);
        NitriteBuilder builder = NitriteBuilder.get();
        builder.filePath(file);
        Nitrite db = builder.openOrCreate();

        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("id1", "value"));

        ObjectRepository<TestObject> repository = db.getRepository(TestObject.class);
        repository.insert(new TestObject("test", 1L));

        ObjectRepository<TestObject> repository2 = db.getRepository(TestObject.class, "key");
        TestObject object = new TestObject();
        object.stringValue = "test2";
        object.longValue = 2L;
        repository2.insert(object);

        ObjectRepository<TestObject2> repository3 = db.getRepository(TestObject2.class, "key");
        TestObject2 object2 = new TestObject2();
        object2.stringValue = "test2";
        object2.longValue = 2L;
        repository3.insert(object2);

        db.commit();
        db.close();

        builder = NitriteBuilder.get();
        builder.filePath(file);
        db = builder.openOrCreate();
        assertTrue(db.hasCollection("test"));
        assertTrue(db.hasRepository(TestObject.class));
        assertTrue(db.hasRepository(TestObject.class, "key"));
        assertFalse(db.hasRepository(TestObject2.class));
        assertTrue(db.hasRepository(TestObject2.class, "key"));
    }


    @Test
    public void testNitriteMapper() {
        NitriteBuilder builder = NitriteBuilder.get();
        builder.loadModule(module(new CustomNitriteMapper()));
        NitriteConfig config = builder.getNitriteConfig();
        assertNotNull(config.nitriteMapper());
    }

    @Test(expected = SecurityException.class)
    public void testOpenOrCreateNullUserId() {
        NitriteBuilder builder = NitriteBuilder.get();
        builder.openOrCreate(null, "abcd");
    }

    @Test(expected = SecurityException.class)
    public void testOpenOrCreateNullPassword() {
        NitriteBuilder builder = NitriteBuilder.get();
        builder.openOrCreate("abcd", null);
    }

    @Test(expected = NitriteIOException.class)
    public void testDbCorruption() throws IOException {
        File file = new File(fakeFile);
        FileWriter writesToFile;
        // Create file writer object
        writesToFile = new FileWriter(file);
        // Wrap the writer with buffered streams
        BufferedWriter writer = new BufferedWriter(writesToFile);
        int line;
        Random rand = new Random();
        for (int j = 0; j < 10; j++) {
            // Randomize an integer and write it to the output file
            line = rand.nextInt(50000);
            writer.write(line + "\n");
        }
        // Close the stream
        writer.close();

        Nitrite fakeDb = NitriteBuilder.get().filePath(fakeFile).openOrCreate();
        assertNull(fakeDb);
    }

    @Test(expected = InvalidOperationException.class)
    public void testDbInMemoryReadonly() {
        Nitrite fakeDb = NitriteBuilder.get()
            .readOnly()
            .openOrCreate();
        assertNull(fakeDb);
    }

    @Test(expected = NitriteIOException.class)
    public void testDbInvalidDirectory() {
        fakeFile = "/tmp/fake/fake.db";
        Nitrite db = NitriteBuilder.get().filePath(fakeFile).openOrCreate("test", "test");
        assertNull(db);
    }

    @Test
    public void testFieldSeparator() {
        Nitrite db = NitriteBuilder.get()
            .filePath(filePath)
            .fieldSeparator("::")
            .openOrCreate();

        Document document = createDocument("firstName", "John")
            .put("colorCodes", new Document[]{createDocument("color", "Red"), createDocument("color", "Green")})
            .put("address", createDocument("street", "ABCD Road"));

        String street = document.get("address::street", String.class);
        assertEquals("ABCD Road", street);

        // use default separator, it should return null
        street = document.get("address.street", String.class);
        assertNull(street);

        assertEquals(document.get("colorCodes::1::color"), "Green");
    }

    @Test(expected = NitriteIOException.class)
    public void testInvalidPath() {
        Nitrite db = NitriteBuilder.get()
            .filePath("http://www.localhost.com")
            .openOrCreate("test", "test");
        assertNull(db);
    }

    private static class CustomIndexer implements Indexer {

        @Override
        public String getIndexType() {
            return "Custom";
        }

        @Override
        public void writeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {

        }

        @Override
        public void removeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {

        }

        @Override
        public void updateIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object newValue, Object oldValue) {

        }

        @Override
        public void dropIndex(NitriteMap<NitriteId, Document> collection, String field) {

        }

        @Override
        public void initialize(NitriteConfig nitriteConfig) {

        }
    }

    public static class CustomNitriteMapper implements NitriteMapper {

        @Override
        public <Source, Target> Target convert(Source source, Class<Target> type) {
            return null;
        }

        @Override
        public boolean isValueType(Class<?> type) {
            return false;
        }

        @Override
        public boolean isValue(Object object) {
            return false;
        }

        @Override
        public void initialize(NitriteConfig nitriteConfig) {

        }
    }

    @Index(value = "longValue")
    private class TestObject implements Mappable {
        private String stringValue;
        private Long longValue;

        public TestObject() {
        }

        public TestObject(String stringValue, Long longValue) {
            this.longValue = longValue;
            this.stringValue = stringValue;
        }

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument("stringValue", stringValue)
                .put("longValue", longValue);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            if (document != null) {
                this.stringValue = document.get("stringValue", String.class);
                this.longValue = document.get("longValue", Long.class);
            }
        }
    }

    @Index(value = "longValue")
    private class TestObject2 implements Mappable {
        private String stringValue;
        private Long longValue;

        public TestObject2() {
        }

        public TestObject2(String stringValue, Long longValue) {
            this.longValue = longValue;
            this.stringValue = stringValue;
        }

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument("stringValue", stringValue)
                .put("longValue", longValue);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            if (document != null) {
                this.stringValue = document.get("stringValue", String.class);
                this.longValue = document.get("longValue", Long.class);
            }
        }
    }
}
