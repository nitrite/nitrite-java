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

import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Set;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;
import static org.dizitart.no2.filters.Filters.ALL;
import static org.junit.Assert.*;

public class NitriteTest {
    private Nitrite db;
    private NitriteCollection collection;
    private SimpleDateFormat simpleDateFormat;
    private String fileName = getRandomTempDbFile();

    @Before
    public void setUp() throws ParseException {
        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate("test-user", "test-password");

        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

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
        collection.insert(doc1, doc2, doc3);
    }

    @After
    public void tearDown() throws IOException {
        if (!collection.isClosed()) {
            collection.remove(ALL);
            collection.close();
        }
        db.close();
        Files.delete(Paths.get(fileName));
    }

    @Test
    public void testListCollectionNames() {
        Set<String> collectionNames = db.listCollectionNames();
        assertEquals(collectionNames.size(), 1);
    }

    @Test
    public void testListRepositories() {
        db.getRepository(getClass());
        Set<String> repositories = db.listRepositories();
        assertEquals(repositories.size(), 1);
    }

    @Test
    public void testHasCollection() {
        assertTrue(db.hasCollection("test"));
        assertFalse(db.hasCollection("lucene" + INTERNAL_NAME_SEPARATOR + "test"));
    }

    @Test
    public void testHasRepository() {
        db.getRepository(getClass());
        assertTrue(db.hasRepository(getClass()));
        assertFalse(db.hasRepository(String.class));
    }

    @Test
    public void testCompact() {
        long initialSize = new File(fileName).length();
        db.compact();
        db.commit();
        db.close();
        // according to documentation MVStore.compactMoveChunks() size would
        // increase temporarily
        assertTrue(new File(fileName).length() > initialSize);
    }

    @Test
    public void testReopen() throws ParseException {
        assertNotNull(db);
        NitriteCollection testCollection = db.getCollection("test");
        assertNotNull(testCollection);
        int prevSize = testCollection.find().size();

        db.close();

        db = null;

        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate("test-user", "test-password");

        assertNotNull(db);
        testCollection = db.getCollection("test");
        assertNotNull(testCollection);
        int sizeNow = testCollection.find().size();
        assertEquals(prevSize, sizeNow);

        db.close();
        db = null;

        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate("test-user", "test-password");
        testCollection = db.getCollection("test");
        testCollection.insert(createDocument("firstName", "fn12")
                .put("lastName", "ln12")
                .put("birthDay", simpleDateFormat.parse("2010-07-01T16:02:48.440Z"))
                .put("data", new byte[]{10, 20, 30})
                .put("body", "a quick brown fox jump over the lazy dog"));

        db.close();
        db = null;

        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate("test-user", "test-password");
        testCollection = db.getCollection("test");
        assertNotNull(testCollection);
        sizeNow = testCollection.find().size();
        assertEquals(prevSize + 1, sizeNow);
    }

    @Test
    public void testCloseImmediately() {
        NitriteCollection testCollection = db.getCollection("test");
        testCollection.insert(createDocument("a", "b"));
        db.closeImmediately();

        assertTrue(testCollection.isClosed());
    }

    @Test
    public void testCloseImmediatelyReadonlyDatabase() {
        db.close();
        db = null;

        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .readOnly()
                .openOrCreate("test-user", "test-password");
        NitriteCollection testCollection = db.getCollection("test");
        testCollection.insert(createDocument("a", "b"));
        db.closeImmediately();

        assertTrue(testCollection.isClosed());
    }

    @Test
    public void testCloseReadonlyDatabase() {
        db.close();
        db = null;

        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .readOnly()
                .openOrCreate("test-user", "test-password");
        NitriteCollection testCollection = db.getCollection("test");
        testCollection.insert(createDocument("a", "b"));
        db.close();

        assertTrue(testCollection.isClosed());
    }

    @Test
    public void testValidateUser() {
        assertTrue(db.validateUser("test-user", "test-password"));
        assertFalse(db.validateUser("test-user1", "test-password"));
        assertFalse(db.validateUser("test-user", "test-password1"));
        assertFalse(db.validateUser("test-user", null));
        assertFalse(db.validateUser(null, null));
    }

    @Test
    public void testGetCollection() {
        NitriteCollection collection = db.getCollection("test-collection");
        assertNotNull(collection);
        assertEquals(collection.getName(), "test-collection");
    }

    @Test
    public void testGetRepository() {
        ObjectRepository<NitriteTest> repository = db.getRepository(NitriteTest.class);
        assertNotNull(repository);
        assertEquals(repository.getType(), NitriteTest.class);
    }

    @Test
    public void testGetRepositoryWithKey() {
        ObjectRepository<NitriteTest> repository = db.getRepository("key", NitriteTest.class);
        assertNotNull(repository);
        assertEquals(repository.getType(), NitriteTest.class);
        assertFalse(db.hasRepository(NitriteTest.class));
        assertTrue(db.hasRepository("key", NitriteTest.class));
    }

    @Test
    public void testMultipleGetCollection() {
        NitriteCollection collection = db.getCollection("test-collection");
        assertNotNull(collection);
        assertEquals(collection.getName(), "test-collection");

        NitriteCollection collection2 = db.getCollection("test-collection");
        assertNotNull(collection2);
        assertEquals(collection2.getName(), "test-collection");
    }

    @Test
    public void testMultipleGetRepository() {
        ObjectRepository<NitriteTest> repository = db.getRepository(NitriteTest.class);
        assertNotNull(repository);
        assertEquals(repository.getType(), NitriteTest.class);

        ObjectRepository<NitriteTest> repository2 = db.getRepository(NitriteTest.class);
        assertNotNull(repository2);
        assertEquals(repository2.getType(), NitriteTest.class);
    }
}
