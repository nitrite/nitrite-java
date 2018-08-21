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

package org.dizitart.no2.tool;

import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.collection.NitriteCollection;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.filters.Filters.ALL;
import static org.dizitart.no2.tool.Recovery.recover;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class RecoveryTest {
    private static final String fileName = getRandomTempDbFile();

    @Test
    public void testRecovery() throws ParseException {
        Nitrite db;
        NitriteCollection collection;
        db = Nitrite.builder()
                .filePath(fileName)
                .compressed()
                .openOrCreate();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

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

        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));
        collection.createIndex("firstName", IndexOptions.indexOptions(IndexType.Unique));
        collection.createIndex("lastName", IndexOptions.indexOptions(IndexType.NonUnique));
        collection.insert(doc1, doc2, doc3);

        db.commit();
        db.close();

        assertTrue(recover(fileName));
    }

    @After
    public void cleanUp() throws IOException {
        Files.delete(Paths.get(fileName));
    }
}
