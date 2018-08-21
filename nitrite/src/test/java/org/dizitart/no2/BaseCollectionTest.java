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

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.WriteResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.filters.Filters.ALL;

@Slf4j
@RunWith(value = Parameterized.class)
public abstract class BaseCollectionTest {
    private String fileName = getRandomTempDbFile();
    protected Nitrite db;
    protected NitriteCollection collection;
    protected Document doc1, doc2, doc3;
    protected SimpleDateFormat simpleDateFormat;

    @Parameterized.Parameter
    public boolean inMemory = false;

    @Parameterized.Parameter(value = 1)
    public boolean isSecured = false;

    @Parameterized.Parameter(value = 2)
    public boolean isCompressed = false;

    @Parameterized.Parameter(value = 3)
    public boolean isAutoCommit = false;

    @Parameterized.Parameter(value = 4)
    public boolean isAutoCompact = false;

    @Parameterized.Parameters(name = "InMemory = {0}, Secured = {1}, " +
            "Compressed = {2}, AutoCommit = {3}, AutoCompact = {4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {false, false, false, false, false},
                {false, false, false, true, false},
                {false, false, true, false, false},
                {false, false, true, true, false},
                {false, true, false, false, false},
                {false, true, false, true, false},
                {false, true, true, false, false},
                {false, true, true, true, false},
                {true, false, false, false, true},
                {true, false, false, true, true},
                {true, false, true, false, true},
                {true, false, true, true, true},
                {true, true, false, false, true},
                {true, true, false, true, true},
                {true, true, true, false, true},
                {true, true, true, true, true},
        });
    }

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() {
        try {
            openDb();

            simpleDateFormat
                    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

            doc1 = createDocument("firstName", "fn1")
                    .put("lastName", "ln1")
                    .put("birthDay", simpleDateFormat.parse("2012-07-01T16:02:48.440Z"))
                    .put("data", new byte[]{1, 2, 3})
                    .put("list", Arrays.asList("one", "two", "three"))
                    .put("body", "a quick brown fox jump over the lazy dog");
            doc2 = createDocument("firstName", "fn2")
                    .put("lastName", "ln2")
                    .put("birthDay", simpleDateFormat.parse("2010-06-12T16:02:48.440Z"))
                    .put("data", new byte[]{3, 4, 3})
                    .put("list", Arrays.asList("three", "four", "three"))
                    .put("body", "quick hello world from nitrite");
            doc3 = createDocument("firstName", "fn3")
                    .put("lastName", "ln2")
                    .put("birthDay", simpleDateFormat.parse("2014-04-17T16:02:48.440Z"))
                    .put("data", new byte[]{9, 4, 8})
                    .put("body", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                            "Sed nunc mi, mattis ullamcorper dignissim vitae, condimentum non lorem.");

            collection = db.getCollection("test");
            collection.remove(ALL);
        } catch (Throwable t) {
            log.error("Error while initializing test database", t);
        }
    }

    @After
    public void clear() {
        try {
            if (collection != null && !collection.isDropped()) {
                collection.close();
            }
            if (db != null) db.close();
            if (!inMemory) {
                Files.delete(Paths.get(fileName));
            }
        } catch (Throwable t) {
            log.error("Error while clearing test database", t);
        }
    }

    private void openDb() {
        NitriteBuilder builder = new NitriteBuilder();

        if (isCompressed) {
            builder.compressed();
        }

        if (!isAutoCommit) {
            builder.disableAutoCommit();
        }

        if (!inMemory) {
            builder.filePath(fileName);
        }

        if (!isAutoCompact) {
            builder.disableAutoCompact();
        }

        if (isSecured) {
            db = builder.openOrCreate("test-user", "test-password");
        } else {
            db = builder.openOrCreate();
        }
    }

    protected WriteResult insert() {
        return collection.insert(doc1, doc2, doc3);
    }
}
