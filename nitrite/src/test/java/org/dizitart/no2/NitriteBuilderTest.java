/*
 *
 * Copyright 2017 Nitrite author or authors.
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

import org.dizitart.no2.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.fulltext.TextIndexingService;
import org.dizitart.no2.fulltext.TextTokenizer;
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
        TextIndexingService textIndexingService = new LuceneService();
        TextTokenizer textTokenizer = new EnglishTextTokenizer();
        String filePath = getRandomTempDbFile();

        NitriteBuilder builder = Nitrite.builder();
        builder.autoCommitBufferSize(1);
        builder.compressed();
        builder.disableAutoCommit();
        builder.disableAutoCompact();
        builder.filePath(filePath);
        builder.textIndexingService(textIndexingService);
        builder.textTokenizer(textTokenizer);

        Nitrite db = builder.openOrCreate();
        NitriteContext context = db.getContext();

        assertEquals(context.getAutoCommitBufferSize(), 1);
        assertEquals(context.getTextIndexingService(), textIndexingService);
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
}
