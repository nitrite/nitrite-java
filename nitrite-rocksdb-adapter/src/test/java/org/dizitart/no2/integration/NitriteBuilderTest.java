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

import org.apache.commons.io.FileUtils;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.EntityConverterMapper;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NitriteSecurityException;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.rocksdb.RocksDBConfig;
import org.dizitart.no2.rocksdb.RocksDBModule;
import org.dizitart.no2.store.StoreConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.module.NitriteModule.module;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteBuilderTest {
    private String fakeFile;
    private String filePath;
    private Nitrite db;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void startup() {
        fakeFile = getRandomTempDbFile();
        filePath = getRandomTempDbFile();
    }

    @After
    public void cleanup() {
        (new NitriteConfig()).fieldSeparator(".");

        if (db != null && !db.isClosed()) {
            db.close();
        }

        if (Files.exists(Paths.get(filePath))) {
            TestUtil.deleteDb(filePath);
        }

        if (Files.exists(Paths.get(fakeFile))) {
            TestUtil.deleteDb(fakeFile);
        }
    }

    @Test
    public void testConfig() {
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(filePath)
            .build();

        NitriteBuilder nitriteBuilder = Nitrite.builder();
        nitriteBuilder.loadModule(module(new CustomIndexer()));
        nitriteBuilder.loadModule(storeModule);

        db = nitriteBuilder.openOrCreate();
        NitriteConfig config = nitriteBuilder.getNitriteConfig();
        RocksDBConfig storeConfig = (RocksDBConfig) db.getStore().getStoreConfig();

        assertEquals(config.findIndexer("Custom").getClass(), CustomIndexer.class);
        assertFalse(storeConfig.isReadOnly());
        assertFalse(storeConfig.isInMemory());
        assertFalse(isNullOrEmpty(storeConfig.filePath()));

        db.close();
        TestUtil.deleteDb(filePath);
    }

    @Test
    public void testConfigWithFile() throws IOException {
        File file = new File(filePath);
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(file)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();
        StoreConfig storeConfig = db.getStore().getStoreConfig();

        assertFalse(storeConfig.isInMemory());
        assertFalse(isNullOrEmpty(storeConfig.filePath()));

        NitriteCollection test = db.getCollection("test");
        assertNotNull(test);

        db.commit();
        db.close();

        FileUtils.deleteDirectory(file.getAbsoluteFile());
    }

    @Test(expected = InvalidOperationException.class)
    public void testConfigWithFileNull() {
        File file = null;
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(file)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();
        StoreConfig storeConfig = db.getStore().getStoreConfig();

        assertTrue(storeConfig.isInMemory());
        assertTrue(isNullOrEmpty(storeConfig.filePath()));

        NitriteCollection test = db.getCollection("test");
        assertNotNull(test);

        db.commit();
        db.close();
    }

    @Test
    public void testPopulateRepositories() {
        File file = new File(filePath);
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(file)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();

        EntityConverterMapper documentMapper = (EntityConverterMapper) db.getConfig().nitriteMapper();
        documentMapper.registerEntityConverter(new TestObject.Converter());
        documentMapper.registerEntityConverter(new TestObject2.Converter());

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

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();
        assertTrue(db.hasCollection("test"));
        assertTrue(db.hasRepository(TestObject.class));
        assertTrue(db.hasRepository(TestObject.class, "key"));
        assertFalse(db.hasRepository(TestObject2.class));
        assertTrue(db.hasRepository(TestObject2.class, "key"));
    }


    @Test
    public void testNitriteMapper() {
        NitriteBuilder builder = Nitrite.builder();
        builder.loadModule(module(new CustomNitriteMapper()));
        NitriteConfig config = builder.getNitriteConfig();
        assertNotNull(config.nitriteMapper());
    }

    @Test(expected = NitriteSecurityException.class)
    public void testOpenOrCreateNullUserId() {
        NitriteBuilder builder = Nitrite.builder();
        builder.openOrCreate(null, "abcd");
    }

    @Test(expected = NitriteSecurityException.class)
    public void testOpenOrCreateNullPassword() {
        NitriteBuilder builder = Nitrite.builder();
        builder.openOrCreate("abcd", null);
    }


    @Test(expected = NitriteIOException.class)
    public void testDbInvalidDirectory() {
        fakeFile = System.getProperty("java.io.tmpdir") + File.separator + "fake" + File.separator + "fake.db";
        db = createDb(fakeFile, "test", "test");
        assertNull(db);
    }

    @Test
    public void testFieldSeparator() {
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(filePath)
            .build();
        db = Nitrite.builder()
            .loadModule(storeModule)
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

    private static class CustomIndexer implements NitriteIndexer {

        @Override
        public String getIndexType() {
            return "Custom";
        }

        @Override
        public void validateIndex(Fields fields) {

        }

        @Override
        public void dropIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {

        }

        @Override
        public void writeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {

        }

        @Override
        public void removeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {

        }

        @Override
        public LinkedHashSet<NitriteId> findByFilter(FindPlan findPlan, NitriteConfig nitriteConfig) {
            return null;
        }

        @Override
        public void initialize(NitriteConfig nitriteConfig) {

        }
    }

    public static class CustomNitriteMapper implements NitriteMapper {

        @Override
        public <Source, Target> Target tryConvert(Source source, Class<Target> type) {
            return null;
        }

        @Override
        public void initialize(NitriteConfig nitriteConfig) {

        }
    }

    @Index(fields = "longValue")
    private static class TestObject {
        private String stringValue;
        private Long longValue;

        public TestObject() {
        }

        public TestObject(String stringValue, Long longValue) {
            this.longValue = longValue;
            this.stringValue = stringValue;
        }

        public static class Converter implements EntityConverter<TestObject> {

            @Override
            public Class<TestObject> getEntityType() {
                return TestObject.class;
            }

            @Override
            public Document toDocument(TestObject entity, NitriteMapper nitriteMapper) {
                return Document.createDocument()
                    .put("stringValue", entity.stringValue)
                    .put("longValue", entity.longValue);
            }

            @Override
            public TestObject fromDocument(Document document, NitriteMapper nitriteMapper) {
                TestObject entity = new TestObject();
                entity.stringValue = document.get("stringValue", String.class);
                entity.longValue = document.get("longValue", Long.class);
                return entity;
            }
        }
    }

    @Index(fields = "longValue")
    private static class TestObject2 {
        private String stringValue;
        private Long longValue;

        public TestObject2() {
        }

        public TestObject2(String stringValue, Long longValue) {
            this.longValue = longValue;
            this.stringValue = stringValue;
        }

        public static class Converter implements EntityConverter<TestObject2> {

            @Override
            public Class<TestObject2> getEntityType() {
                return TestObject2.class;
            }

            @Override
            public Document toDocument(TestObject2 entity, NitriteMapper nitriteMapper) {
                return createDocument("stringValue", entity.stringValue)
                    .put("longValue", entity.longValue);
            }

            @Override
            public TestObject2 fromDocument(Document document, NitriteMapper nitriteMapper) {
                TestObject2 entity = new TestObject2();
                if (document != null) {
                    entity.stringValue = document.get("stringValue", String.class);
                    entity.longValue = document.get("longValue", Long.class);
                }
                return entity;
            }
        }
    }
}
