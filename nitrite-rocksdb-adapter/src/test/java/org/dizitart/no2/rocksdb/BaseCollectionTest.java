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

package org.dizitart.no2.rocksdb;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.rocksdb.formatter.KryoObjectFormatter;
import org.dizitart.no2.rocksdb.serializers.JodaTimeKryoKeySerializer;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.rocksdb.DbTestOperations.getRandomTempDbFile;

@Slf4j
@RunWith(value = Parameterized.class)
public abstract class BaseCollectionTest {
    @Parameterized.Parameter
    public boolean isSecured = false;

    protected Nitrite db;
    protected NitriteCollection collection;
    protected Document doc1, doc2, doc3;
    protected SimpleDateFormat simpleDateFormat;
    private final String fileName = getRandomTempDbFile();
    protected final KryoObjectFormatter fstMarshaller = new KryoObjectFormatter();

    @Parameterized.Parameters(name = "Secured = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {false},
                {true},
        });
    }

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
            if (db != null && !db.isClosed()) db.close();
            FileUtils.deleteDirectory(new File(fileName));
        } catch (Throwable t) {
            log.error("Error while clearing test database", t);
        }
    }

    private void openDb() {
        fstMarshaller.registerSerializer(DateTime.class, new JodaTimeKryoKeySerializer());

        RocksDBModule storeModule = RocksDBModule.withConfig()
                .filePath(fileName)
                .objectFormatter(fstMarshaller)
                .build();

        if (isSecured) {
            db = Nitrite.builder()
                    .fieldSeparator(".")
                    .loadModule(storeModule)
                    .openOrCreate("test-user", "test-password");
        } else {
            db = Nitrite.builder()
                    .fieldSeparator(".")
                    .loadModule(storeModule)
                    .openOrCreate();
        }
    }

    protected WriteResult insert() {
        return collection.insert(doc1, doc2, doc3);
    }
}
