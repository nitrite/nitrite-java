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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.SimpleDocumentMapper;
import org.dizitart.no2.common.module.NitriteModule;
import org.dizitart.no2.common.module.NitritePlugin;
import org.dizitart.no2.common.module.PluginManager;
import org.dizitart.no2.exceptions.NitriteSecurityException;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.memory.InMemoryConfig;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.module.NitriteModule.module;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NitriteBuilderTest {
    private Nitrite db;

    @Rule
    public Retry retry = new Retry(3);

    @After
    public void cleanup() {
        (new NitriteConfig()).fieldSeparator(".");

        if (db != null && !db.isClosed()) {
            db.close();
        }
    }

    @Test
    public void testConstructor() {
        NitriteConfig nitriteConfig = (new NitriteBuilder()).getNitriteConfig();
        assertTrue(nitriteConfig.getMigrations().isEmpty());
        assertFalse(nitriteConfig.configured);
        assertEquals(1, nitriteConfig.getSchemaVersion().intValue());
        assertNull(nitriteConfig.getNitriteStore());
        PluginManager pluginManager = nitriteConfig.getPluginManager();
        assertSame(nitriteConfig, pluginManager.getNitriteConfig());
        assertTrue(pluginManager.getIndexerMap().isEmpty());
    }

    @Test
    public void testFieldSeparator() {
        NitriteBuilder builderResult = Nitrite.builder();
        assertSame(builderResult, builderResult.fieldSeparator("Separator"));

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

    @Test
    public void testLoadModule() {
        NitriteBuilder builderResult = Nitrite.builder();
        NitriteModule nitriteModule = mock(NitriteModule.class);
        when(nitriteModule.plugins()).thenReturn(new HashSet<NitritePlugin>());
        assertSame(builderResult, builderResult.loadModule(nitriteModule));
        verify(nitriteModule, times(2)).plugins();
    }

    @Test
    public void testAddMigrations() {
        NitriteBuilder builderResult = Nitrite.builder();
        assertSame(builderResult, builderResult.addMigrations(null, null, null));
    }

    @Test
    public void testAddMigrations2() {
        NitriteBuilder builderResult = Nitrite.builder();
        Migration migration = mock(Migration.class);
        when(migration.getToVersion()).thenReturn(1);
        when(migration.getFromVersion()).thenReturn(1);
        assertSame(builderResult, builderResult.addMigrations(migration, null, null));
        verify(migration).getToVersion();
        verify(migration).getFromVersion();
    }

    @Test
    public void testSchemaVersion() {
        NitriteBuilder builderResult = Nitrite.builder();
        assertSame(builderResult, builderResult.schemaVersion(1));
    }

    @Test
    public void testOpenOrCreate() {
        Nitrite actualOpenOrCreateResult = Nitrite.builder().openOrCreate("janedoe", "iloveyou");
        PluginManager pluginManager = actualOpenOrCreateResult.getConfig().getPluginManager();
        NitriteStore<?> store = actualOpenOrCreateResult.getStore();
        assertFalse(actualOpenOrCreateResult.isClosed());
        assertFalse(store.isClosed());
        assertSame(store, pluginManager.getNitriteStore());
        assertTrue(pluginManager.getNitriteMapper() instanceof SimpleDocumentMapper);
    }

    @Test
    public void testOpenOrCreate2() {
        Nitrite actualOpenOrCreateResult = Nitrite.builder().openOrCreate();
        assertFalse(actualOpenOrCreateResult.isClosed());
        NitriteConfig config = actualOpenOrCreateResult.getConfig();
        assertTrue(config.configured);
        NitriteStore<?> store = actualOpenOrCreateResult.getStore();
        assertTrue(store.getRepositoryRegistry().isEmpty());
        assertFalse(store.isClosed());
        PluginManager pluginManager = config.getPluginManager();
        assertEquals(3, pluginManager.getIndexerMap().size());
        assertTrue(pluginManager.getNitriteMapper() instanceof SimpleDocumentMapper);
        assertTrue(store.getCatalog().getKeyedRepositoryNames().isEmpty());
        assertSame(store, pluginManager.getNitriteStore());
        assertTrue(((InMemoryConfig) store.getStoreConfig()).eventListeners().isEmpty());
    }

    @Test
    public void testOpenOrCreate3() {
        Nitrite actualOpenOrCreateResult = Nitrite.builder().openOrCreate("janedoe", "iloveyou");
        assertFalse(actualOpenOrCreateResult.isClosed());
        NitriteConfig config = actualOpenOrCreateResult.getConfig();
        assertTrue(config.configured);
        NitriteStore<?> store = actualOpenOrCreateResult.getStore();
        assertTrue(store.getRepositoryRegistry().isEmpty());
        assertFalse(store.isClosed());
        PluginManager pluginManager = config.getPluginManager();
        assertEquals(3, pluginManager.getIndexerMap().size());
        assertTrue(pluginManager.getNitriteMapper() instanceof SimpleDocumentMapper);
        assertTrue(store.getCatalog().getKeyedRepositoryNames().isEmpty());
        assertSame(store, pluginManager.getNitriteStore());
        assertTrue(((InMemoryConfig) store.getStoreConfig()).eventListeners().isEmpty());
    }

    @Test(expected = NitriteSecurityException.class)
    public void testOpenOrCreate4() {
        NitriteBuilder builderResult = Nitrite.builder();
        builderResult.openOrCreate("", "iloveyou");
        NitriteConfig nitriteConfig = builderResult.getNitriteConfig();
        PluginManager pluginManager = nitriteConfig.getPluginManager();
        assertEquals(3, pluginManager.getIndexerMap().size());
        NitriteStore<?> nitriteStore = nitriteConfig.getNitriteStore();
        assertSame(nitriteStore, pluginManager.getNitriteStore());
        assertTrue(pluginManager.getNitriteMapper() instanceof SimpleDocumentMapper);
        assertFalse(nitriteStore.isClosed());
        assertTrue(((InMemoryConfig) nitriteStore.getStoreConfig()).eventListeners().isEmpty());
    }

    @Test(expected = NitriteSecurityException.class)
    public void testOpenOrCreate5() {
        NitriteBuilder builderResult = Nitrite.builder();
        builderResult.openOrCreate("", "iloveyou");
        NitriteConfig nitriteConfig = builderResult.getNitriteConfig();
        PluginManager pluginManager = nitriteConfig.getPluginManager();
        NitriteStore<?> nitriteStore = nitriteConfig.getNitriteStore();
        assertFalse(nitriteStore.isClosed());
        assertSame(nitriteStore, pluginManager.getNitriteStore());
        assertTrue(pluginManager.getNitriteMapper() instanceof SimpleDocumentMapper);
    }

    @Test
    public void testConfig() {

        NitriteBuilder nitriteBuilder = Nitrite.builder();
        nitriteBuilder.loadModule(module(new CustomIndexer()));

        db = nitriteBuilder.openOrCreate();
        NitriteConfig config = nitriteBuilder.getNitriteConfig();

        assertEquals(config.findIndexer("Custom").getClass(), CustomIndexer.class);

        db.close();
    }

    @Test
    public void testConfigWithFile() {
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
    public void testConfigWithFileNull() {
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
        public void initialize(NitriteConfig nitriteConfig) {

        }
    }

}

