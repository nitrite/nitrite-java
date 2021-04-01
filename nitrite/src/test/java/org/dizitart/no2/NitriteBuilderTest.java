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
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.module.PluginManager;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreConfig;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.LinkedHashSet;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.module.NitriteModule.module;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteBuilderTest {
    private Nitrite db;

    @Rule
    public Retry retry = new Retry(3);

    @After
    public void cleanup() throws Exception {
        (new NitriteConfig()).fieldSeparator(".");

        if (db != null && !db.isClosed()) {
            db.close();
        }
    }

    @Test
    public void testConfig() throws Exception {

        NitriteBuilder nitriteBuilder = Nitrite.builder();
        nitriteBuilder.loadModule(module(new CustomIndexer()));

        db = nitriteBuilder.openOrCreate();
        NitriteConfig config = nitriteBuilder.getNitriteConfig();

        assertEquals(config.findIndexer("Custom").getClass(), CustomIndexer.class);

        db.close();
    }

    @Test
    public void testConfigWithFile() throws Exception {
        db = Nitrite.builder()
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
    public void testConfigWithFileNull() throws Exception {
        db = Nitrite.builder().openOrCreate();
        StoreConfig storeConfig = db.getStore().getStoreConfig();

        assertTrue(storeConfig.isInMemory());
        assertTrue(isNullOrEmpty(storeConfig.filePath()));

        NitriteCollection test = db.getCollection("test");
        assertNotNull(test);

        db.commit();
        db.close();
    }

    @Test
    public void testNitriteMapper() {
        NitriteBuilder builder = Nitrite.builder();
        builder.loadModule(module(new CustomNitriteMapper()));
        NitriteConfig config = builder.getNitriteConfig();
        assertNotNull(config.nitriteMapper());
    }

    @Test(expected = SecurityException.class)
    public void testOpenOrCreateNullUserId() {
        NitriteBuilder builder = Nitrite.builder();
        builder.openOrCreate(null, "abcd");
    }

    @Test
    public void testOpenOrCreate() {
        Nitrite actualOpenOrCreateResult = Nitrite.builder().openOrCreate("janedoe", "iloveyou");
        PluginManager pluginManager = actualOpenOrCreateResult.getConfig().getPluginManager();
        NitriteStore<?> store = actualOpenOrCreateResult.getStore();
        assertFalse(actualOpenOrCreateResult.isClosed());
        assertFalse(store.isClosed());
        assertSame(store, pluginManager.getNitriteStore());
        assertTrue(pluginManager.getNitriteMapper() instanceof org.dizitart.no2.mapper.MappableMapper);
    }

    @Test(expected = SecurityException.class)
    public void testOpenOrCreate2() {
        NitriteBuilder builderResult = Nitrite.builder();
        builderResult.openOrCreate("", "iloveyou");
        NitriteConfig nitriteConfig = builderResult.getNitriteConfig();
        PluginManager pluginManager = nitriteConfig.getPluginManager();
        NitriteStore<?> nitriteStore = nitriteConfig.getNitriteStore();
        assertFalse(nitriteStore.isClosed());
        assertSame(nitriteStore, pluginManager.getNitriteStore());
        assertTrue(pluginManager.getNitriteMapper() instanceof org.dizitart.no2.mapper.MappableMapper);
    }

    @Test(expected = SecurityException.class)
    public void testOpenOrCreateNullPassword() {
        NitriteBuilder builder = Nitrite.builder();
        builder.openOrCreate("abcd", null);
    }

    @Test
    public void testFieldSeparator() {
        db = Nitrite.builder()
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
    private static class TestObject implements Mappable {
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
    private static class TestObject2 implements Mappable {
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
