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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.index.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.index.fulltext.TextTokenizer;
import org.dizitart.no2.services.LuceneService;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteBuilderTest {

    @Test
    public void testConfig() throws IOException {
        TextIndexer textIndexer = new LuceneService();
        TextTokenizer textTokenizer = new EnglishTextTokenizer();
        String filePath = getRandomTempDbFile();

        NitriteBuilder builder = Nitrite.builder();
        builder.autoCommitBufferSize(1);
        builder.compressed();
        builder.disableAutoCommit();
        builder.disableAutoCompact();
        builder.filePath(filePath);
        builder.textIndexer(textIndexer);
        builder.textTokenizer(textTokenizer);

        Nitrite db = builder.openOrCreate();
        NitriteContext context = db.getContext();

        assertEquals(context.getAutoCommitBufferSize(), 1);
        assertEquals(context.getTextIndexer(), textIndexer);
        assertEquals(context.getTextTokenizer(), textTokenizer);
        assertFalse(context.isAutoCommitEnabled());
        assertFalse(context.isAutoCompactEnabled());
        assertTrue(context.isCompressed());
        assertFalse(context.isReadOnly());
        assertFalse(context.isInMemory());
        assertFalse(isNullOrEmpty(context.getFilePath()));

        db.close();

        db = Nitrite.builder()
                .readOnly()
                .filePath(filePath)
                .openOrCreate();
        context = db.getContext();
        assertTrue(context.isReadOnly());
        db.close();

        Files.delete(Paths.get(filePath));
    }

    @Test
    public void testConfigWithFile() {
        File file = new File(getRandomTempDbFile());
        NitriteBuilder builder = Nitrite.builder();
        builder.filePath(file);
        Nitrite db = builder.openOrCreate();
        NitriteContext context = db.getContext();

        assertFalse(context.isInMemory());
        assertFalse(isNullOrEmpty(context.getFilePath()));

        NitriteCollection test = db.getCollection("test");
        assertNotNull(test);

        db.commit();
        db.close();

        assertTrue(file.delete());
    }

    @Test
    public void testConfigWithFileNull() {
        File file = null;
        NitriteBuilder builder = Nitrite.builder();
        builder.filePath(file);
        Nitrite db = builder.openOrCreate();
        NitriteContext context = db.getContext();

        assertTrue(context.isInMemory());
        assertTrue(isNullOrEmpty(context.getFilePath()));

        NitriteCollection test = db.getCollection("test");
        assertNotNull(test);

        db.commit();
        db.close();
    }

    @Test
    public void testPopulateRepositories() {
        File file = new File(getRandomTempDbFile());
        NitriteBuilder builder = Nitrite.builder();
        builder.filePath(file);
        Nitrite db = builder.openOrCreate();

        NitriteCollection collection = db.getCollection("test");
        collection.insert(Document.createDocument("id1", "value"));

        ObjectRepository<TestObject> repository = db.getRepository(TestObject.class);
        repository.insert(new TestObject("test", 1L));

        ObjectRepository<TestObject> repository2 = db.getRepository("key", TestObject.class);
        TestObject object = new TestObject();
        object.stringValue = "test2";
        object.longValue = 2L;
        repository2.insert(object);

        ObjectRepository<TestObject2> repository3 = db.getRepository("key", TestObject2.class);
        TestObject2 object2 = new TestObject2();
        object2.stringValue = "test2";
        object2.longValue = 2L;
        repository3.insert(object2);

        db.commit();
        db.close();

        db = builder.openOrCreate();
        assertTrue(db.hasCollection("test"));
        assertTrue(db.hasRepository(TestObject.class));
        assertTrue(db.hasRepository("key", TestObject.class));
        assertFalse(db.hasRepository(TestObject2.class));
        assertTrue(db.hasRepository("key", TestObject2.class));
    }

    @Test
    public void testRegisterModule() {
        TestModule testModule = new TestModule();
        File file = new File(getRandomTempDbFile());
        NitriteBuilder builder = Nitrite
                .builder()
                .filePath(file)
                .registerModule(testModule);
        Nitrite db = builder.openOrCreate();
        NitriteContext context = db.getContext();

        assertTrue(context.getRegisteredModules().contains(testModule));
    }

    @Index(value = "longValue")
    private class TestObject {
        private String stringValue;
        private Long longValue;

        public TestObject() {}

        public TestObject(String stringValue, Long longValue) {
            this.longValue = longValue;
            this.stringValue = stringValue;
        }
    }

    @Index(value = "longValue")
    private class TestObject2 {
        private String stringValue;
        private Long longValue;

        public TestObject2() {}

        public TestObject2(String stringValue, Long longValue) {
            this.longValue = longValue;
            this.stringValue = stringValue;
        }
    }

    private class TestModule extends Module {

        @Override
        public String getModuleName() {
            return "TestModule";
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public void setupModule(SetupContext context) {

        }
    }
}
