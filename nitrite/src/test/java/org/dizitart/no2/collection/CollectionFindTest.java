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

package org.dizitart.no2.collection;

import org.dizitart.no2.BaseCollectionTest;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.joda.time.DateTime;
import org.junit.Test;

import java.text.Collator;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.DocumentUtils.isSimilar;
import static org.dizitart.no2.common.util.TestUtil.isSorted;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.FluentFilter.$;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CollectionFindTest extends BaseCollectionTest {

    private static Document trimMeta(Document document) {
        document.remove(DOC_ID);
        document.remove(DOC_REVISION);
        document.remove(DOC_MODIFIED);
        document.remove(DOC_SOURCE);
        return document;
    }

    @Test
    public void testFindAll() {
        insert();

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testFindWithFilter() throws ParseException {
        insert();

        DocumentCursor cursor = collection.find(where("birthDay").gt(
            simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("birthDay").gte(
            simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("birthDay").lt(
            simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("birthDay").lte(
            simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("birthDay").lte(
            new Date()));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(where("birthDay").lt(
            new Date()));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(where("birthDay").gt(
            new Date()));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(where("birthDay").gte(
            new Date()));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(
            where("birthDay").lte(new Date())
                .and(where("firstName").eq("fn1")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
            where("birthDay").lte(new Date())
                .or(where("firstName").eq("fn12")));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(
            where("birthDay").lte(new Date())
                .or(where("firstName").eq("fn12"))
                .and(where("lastName").eq("ln1")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
            where("birthDay").lte(new Date())
                .or(where("firstName").eq("fn12"))
                .and(where("lastName").eq("ln1")).not());
        assertEquals(cursor.size(), 2);


        cursor = collection.find(where("data.1").eq((byte) 4));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("data.1").lt(4));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("lastName").in("ln1", "ln2", "ln10"));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(where("firstName").notIn("fn1", "fn2"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(ALL.not());
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testFindWithSkipLimit() {
        insert();

        DocumentCursor cursor = collection.find().skipLimit(0, 1);
        assertEquals(cursor.size(), 1);

        cursor = collection.find().skipLimit(1, 3);
        assertEquals(cursor.size(), 2);

        cursor = collection.find().skipLimit(0, 30);
        assertEquals(cursor.size(), 3);

        cursor = collection.find().skipLimit(2, 3);
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testFindWithSkip() {
        insert();

        DocumentCursor cursor = collection.find().skip(0);
        assertEquals(cursor.size(), 3);

        cursor = collection.find().skip(1);
        assertEquals(cursor.size(), 2);

        cursor = collection.find().skip(30);
        assertEquals(cursor.size(), 0);

        cursor = collection.find().skip(2);
        assertEquals(cursor.size(), 1);

        boolean invalid = false;
        try {
            cursor = collection.find().skip(-1);
            assertEquals(cursor.size(), 1);
        } catch (ValidationException e) {
            invalid = true;
        }
        assertTrue(invalid);
    }

    @Test
    public void testFindWithLimit() {
        insert();

        DocumentCursor cursor = collection.find().limit(0);
        assertEquals(cursor.size(), 0);

        cursor = collection.find().limit(1);
        assertEquals(cursor.size(), 1);

        boolean invalid = false;
        try {
            cursor = collection.find().limit(-1);
            assertEquals(cursor.size(), 1);
        } catch (ValidationException e) {
            invalid = true;
        }
        assertTrue(invalid);

        cursor = collection.find().limit(30);
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testFindSortAscending() {
        insert();

        DocumentCursor cursor = collection.find().sort("birthDay", SortOrder.Ascending);
        assertEquals(cursor.size(), 3);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, true));
    }

    @Test
    public void testFindSortDescending() {
        insert();

        DocumentCursor cursor = collection.find().sort("birthDay", SortOrder.Descending);
        assertEquals(cursor.size(), 3);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, false));
    }

    @Test
    public void testFindLimitAndSort() {
        insert();

        DocumentCursor cursor = collection.find().
            sort("birthDay", SortOrder.Descending).skipLimit(1, 2);
        assertEquals(cursor.size(), 2);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, false));

        cursor = collection.find().
            sort("birthDay", SortOrder.Ascending).skipLimit(1, 2);
        assertEquals(cursor.size(), 2);
        dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, true));

        cursor = collection.find().
            sort("firstName", SortOrder.Ascending).skipLimit(0, 30);
        assertEquals(cursor.size(), 3);
        List<String> nameList = new ArrayList<>();
        for (Document document : cursor) {
            nameList.add(document.get("firstName", String.class));
        }
        assertTrue(isSorted(nameList, true));
    }

    @Test
    public void testFindSortOnNonExistingField() {
        insert();
        DocumentCursor cursor = collection.find().sort("my-value", SortOrder.Descending);
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testFindInvalidField() {
        insert();
        DocumentCursor cursor = collection.find(where("myField").eq("myData"));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testFindInvalidFieldWithInvalidAccessor() {
        insert();
        DocumentCursor cursor = collection.find(where("myField.0").eq("myData"));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testFindLimitAndSortInvalidField() {
        insert();
        DocumentCursor cursor = collection.find().
            sort("birthDay2", SortOrder.Descending).skipLimit(1, 2);
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testGetById() {
        collection.insert(doc1);
        NitriteId id = NitriteId.createId("1");
        Document document = collection.getById(id);
        assertNull(document);

        document = collection.find().firstOrNull();

        assertEquals(document.get(DOC_ID), document.getId().getIdValue());
        assertEquals(document.get("firstName"), "fn1");
        assertEquals(document.get("lastName"), "ln1");
        assertArrayEquals((byte[]) document.get("data"), new byte[]{1, 2, 3});
        assertEquals(document.get("body"), "a quick brown fox jump over the lazy dog");
    }

    @Test
    public void testFindWithFilterAndOption() {
        insert();
        DocumentCursor cursor = collection.find(where("birthDay").lte(new Date())).
            sort("firstName", SortOrder.Ascending).skipLimit(1, 2);
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testFindTextWithRegex() {
        insert();
        DocumentCursor cursor = collection.find(where("body").regex("hello"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("body").regex("test"));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(where("body").regex("^hello$"));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(where("body").regex(".*"));
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testProject() {
        insert();
        DocumentCursor cursor = collection.find(where("birthDay").lte(new Date())).
            sort("firstName", SortOrder.Ascending).skipLimit(0, 3);
        int iteration = 0;
        for (Document document : cursor) {
            switch (iteration) {
                case 0:
                    assertTrue(isSimilar(document, doc1, "firstName", "lastName", "birthDay", "data", "list", "body"));
                    break;
                case 1:
                    assertTrue(isSimilar(document, doc2, "firstName", "lastName", "birthDay", "data", "list", "body"));
                    break;
                case 2:
                    assertTrue(isSimilar(document, doc3, "firstName", "lastName", "birthDay", "data", "list", "body"));
                    break;
            }
            iteration++;
        }
        assertEquals(iteration, 3);
    }

    @Test
    public void testProjectWithCustomDocument() {
        insert();
        DocumentCursor cursor = collection.find(where("birthDay").lte(new Date())).
            sort("firstName", SortOrder.Ascending).skipLimit(0, 3);

        Document projection = createDocument("firstName", null)
            .put("lastName", null);

        Iterable<Document> documents = cursor.project(projection);
        int iteration = 0;
        for (Document document : documents) {
            assertTrue(document.containsKey("firstName"));
            assertTrue(document.containsKey("lastName"));

            assertFalse(document.containsKey("_id"));
            assertFalse(document.containsKey("birthDay"));
            assertFalse(document.containsKey("data"));
            assertFalse(document.containsKey("body"));

            switch (iteration) {
                case 0:
                    assertEquals(document.get("firstName"), "fn1");
                    assertEquals(document.get("lastName"), "ln1");
                    break;
                case 1:
                    assertEquals(document.get("firstName"), "fn2");
                    assertEquals(document.get("lastName"), "ln2");
                    break;
                case 2:
                    assertEquals(document.get("firstName"), "fn3");
                    assertEquals(document.get("lastName"), "ln2");
                    break;
            }
            iteration++;
        }
        assertEquals(iteration, 3);
    }

    @Test
    public void testFindWithArrayEqual() {
        insert();
        DocumentCursor ids = collection.find(where("data").eq(new byte[]{3, 4, 3}));
        assertNotNull(ids);
        assertEquals(ids.size(), 1);
    }

    @Test
    public void testFindWithArrayEqualFailForWrongCardinality() {
        insert();
        DocumentCursor ids = collection.find(where("data").eq(new byte[]{4, 3, 3}));
        assertNotNull(ids);
        assertEquals(ids.size(), 0);
    }

    @Test
    public void testFindWithIterableEqual() {
        insert();
        DocumentCursor ids = collection.find(where("list").eq(
            new ArrayList<String>() {{
                add("three");
                add("four");
                add("three");
            }}));
        assertNotNull(ids);
        assertEquals(ids.size(), 1);
    }

    @Test
    public void testFindWithIterableEqualFailForWrongCardinality() {
        insert();
        DocumentCursor ids = collection.find(where("list").eq(
            new ArrayList<String>() {{
                add("four");
                add("three");
                add("three");
            }}));
        assertNotNull(ids);
        assertEquals(ids.size(), 0);
    }

    @Test
    public void testFindInArray() {
        insert();
        DocumentCursor ids = collection.find(where("data").elemMatch($.gte(2).and($.lt(5))));
        assertNotNull(ids);
        assertEquals(ids.size(), 3);

        ids = collection.find(where("data").elemMatch($.gt(2).or($.lte(5))));
        assertNotNull(ids);
        assertEquals(ids.size(), 3);

        ids = collection.find(where("data").elemMatch($.gt(1).and($.lt(4))));
        assertNotNull(ids);
        assertEquals(ids.size(), 2);
    }

    @Test
    public void testFindInList() {
        insert();
        DocumentCursor ids = collection.find(where("list").elemMatch($.regex("three")));
        assertNotNull(ids);
        assertEquals(ids.size(), 2);

        ids = collection.find(where("list").elemMatch($.regex("hello")));
        assertNotNull(ids);
        assertEquals(ids.size(), 0);

        ids = collection.find(where("list").elemMatch($.regex("hello").not()));
        assertNotNull(ids);
        assertEquals(ids.size(), 2);
    }

    @Test
    public void testElemMatchFilter() {
        Document doc1 = createDocument("productScores", new Document[]{
            createDocument("product", "abc").put("score", 10),
            createDocument("product", "xyz").put("score", 5)
        }).put("strArray", new String[]{"a", "b"});

        Document doc2 = createDocument("productScores", new Document[]{
            createDocument("product", "abc").put("score", 8),
            createDocument("product", "xyz").put("score", 7)
        }).put("strArray", new String[]{"d", "e"});

        Document doc3 = createDocument("productScores", new Document[]{
            createDocument("product", "abc").put("score", 7),
            createDocument("product", "xyz").put("score", 8)
        }).put("strArray", new String[]{"a", "f"});

        NitriteCollection prodCollection = db.getCollection("prodScore");
        prodCollection.insert(doc1, doc2, doc3);

        List<Document> documentList = prodCollection.find(where("productScores")
            .elemMatch(where("product").eq("xyz").and(where("score").gte(8)))).toList();

        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(where("productScores")
            .elemMatch(where("score").lte(8).not())).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(where("productScores")
            .elemMatch(where("product").eq("xyz").or(where("score").gte(8)))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(where("productScores")
            .elemMatch(where("product").eq("xyz"))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(where("productScores")
            .elemMatch(where("score").gte(10))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(where("productScores")
            .elemMatch(where("score").gt(8))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(where("productScores")
            .elemMatch(where("score").lt(7))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(where("productScores")
            .elemMatch(where("score").lte(7))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(where("productScores")
            .elemMatch(where("score").in(7, 8))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(where("productScores")
            .elemMatch(where("score").notIn(7, 8))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(where("productScores")
            .elemMatch(where("product").regex("xyz"))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(where("strArray")
            .elemMatch($.eq("a"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(where("strArray")
            .elemMatch($.eq("a").or($.eq("f").or($.eq("b"))).not())).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(where("strArray")
            .elemMatch($.gt("e"))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(where("strArray")
            .elemMatch($.gte("e"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(where("strArray")
            .elemMatch($.lte("b"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(where("strArray")
            .elemMatch($.lt("a"))).toList();
        assertEquals(documentList.size(), 0);

        documentList = prodCollection.find(where("strArray")
            .elemMatch($.in("a", "f"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(where("strArray")
            .elemMatch($.regex("a"))).toList();
        assertEquals(documentList.size(), 2);

    }

    @Test
    public void testNotEqualFilter() {
        Document document = createDocument("abc", "123");
        document.put("xyz", null);

        collection.insert(document);
        DocumentCursor cursor = collection.find(where("abc").eq("123"));
        assertEquals(cursor.size(), 1);
        assertEquals(cursor.toList().size(), 1);

        cursor = collection.find(where("xyz").eq(null));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("abc").eq(null).not());
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("abc").eq(null).not().and(where("xyz").eq(null)));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("abc").eq(null).and(where("xyz").eq(null).not()));
        assertEquals(cursor.size(), 0);

        collection.remove(ALL);

        document = createDocument("field", "two");
        document.put(DOC_REVISION, 1482225343161L);

        collection.insert(document);
        Document projection = collection.find(
            where(DOC_REVISION).gte(1482225343160L)
                .and(where(DOC_REVISION).lte(1482225343162L)
                    .and(where(DOC_REVISION).eq(null).not())))
            .firstOrNull();

        assertNull(projection);
    }

    @Test
    public void testFilterAll() {
        DocumentCursor cursor = collection.find(ALL);
        assertNotNull(cursor);
        assertEquals(cursor.size(), 0);

        insert();
        cursor = collection.find(ALL);
        assertNotNull(cursor);
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testIssue72() {
        NitriteCollection coll = db.getCollection("test");
        coll.createIndex("id", IndexOptions.indexOptions(IndexType.Unique));
        coll.createIndex("group", IndexOptions.indexOptions(IndexType.NonUnique));

        coll.remove(ALL);

        Document doc = createDocument().put("id", "test-1").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        doc = createDocument().put("id", "test-2").put("group", "groupA").put("startTime", DateTime.now());
        assertEquals(1, coll.insert(doc).getAffectedCount());

        DocumentCursor cursor = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending);
        assertEquals(2, cursor.size());
        assertNull(cursor.toList().get(1).get("startTime"));
        assertNotNull(cursor.toList().get(0).get("startTime"));

        cursor = coll.find(where("group").eq("groupA")).sort("startTime", SortOrder.Ascending);
        assertEquals(2, cursor.size());
        assertNull(cursor.toList().get(0).get("startTime"));
        assertNotNull(cursor.toList().get(1).get("startTime"));
    }

    @Test
    public void testIssue93() {
        NitriteCollection coll = db.getCollection("orderByOnNullableColumn2");

        coll.remove(ALL);

        Document doc = createDocument().put("id", "test-2").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        doc = createDocument().put("id", "test-1").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        DocumentCursor cursor = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending);
        assertEquals(2, cursor.size());
    }

    @Test
    public void testNullOrderWithAllNull() {
        NitriteCollection coll = db.getCollection("test");

        coll.remove(ALL);

        Document doc = createDocument().put("id", "test-2").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        doc = createDocument().put("id", "test-1").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        DocumentCursor cursor = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending);
        assertEquals(2, cursor.size());

        DocumentCursor cursor2 = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.Default);
        assertEquals(2, cursor2.size());

        assertThat(cursor.toList(), is(cursor2.toList()));

        DocumentCursor cursor3 = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.First);
        assertEquals(2, cursor3.size());

        assertThat(cursor.toList(), is(cursor3.toList()));

        DocumentCursor cursor4 = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.Last);
        assertEquals(2, cursor4.size());

        assertThat(cursor.toList(), is(cursor4.toList()));

        DocumentCursor cursor5 = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.Last);
        assertEquals(2, cursor5.size());

        assertThat(cursor.toList(), is(cursor5.toList()));

        DocumentCursor cursor6 = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.First);
        assertEquals(2, cursor6.size());

        assertThat(cursor.toList(), is(cursor6.toList()));

        DocumentCursor cursor7 = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.Default);
        assertEquals(2, cursor7.size());

        assertThat(cursor.toList(), is(cursor7.toList()));
    }

    @Test
    public void testNullOrder() {
        NitriteCollection coll = db.getCollection("test");
        try {
            coll.createIndex("startTime", IndexOptions.indexOptions(IndexType.NonUnique));
        } catch (IndexingException e) {
            // ignore
        }

        coll.remove(ALL);

        Document doc1 = createDocument().put("id", "test-1").put("group", "groupA");
        assertEquals(1, coll.insert(doc1).getAffectedCount());

        Document doc2 = createDocument().put("id", "test-2").put("group", "groupA").put("startTime", DateTime.now());
        assertEquals(1, coll.insert(doc2).getAffectedCount());

        Document doc3 = createDocument().put("id", "test-3").put("group", "groupA").put("startTime", DateTime.now().plusMinutes(1));
        assertEquals(1, coll.insert(doc3).getAffectedCount());

        DocumentCursor cursor = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc3, doc2, doc1),
            is(cursor.toList().stream().map(CollectionFindTest::trimMeta).collect(Collectors.toList())));

        cursor = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.First);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc1, doc3, doc2),
            is(cursor.toList().stream().map(CollectionFindTest::trimMeta).collect(Collectors.toList())));

        cursor = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.Default);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc3, doc2, doc1),
            is(cursor.toList().stream().map(CollectionFindTest::trimMeta).collect(Collectors.toList())));

        cursor = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.Last);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc3, doc2, doc1),
            is(cursor.toList().stream().map(CollectionFindTest::trimMeta).collect(Collectors.toList())));

        cursor = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.First);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc1, doc2, doc3),
            is(cursor.toList().stream().map(CollectionFindTest::trimMeta).collect(Collectors.toList())));

        cursor = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.Default);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc1, doc2, doc3),
            is(cursor.toList().stream().map(CollectionFindTest::trimMeta).collect(Collectors.toList())));

        cursor = coll.find(where("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.Last);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc2, doc3, doc1),
            is(cursor.toList().stream().map(CollectionFindTest::trimMeta).collect(Collectors.toList())));
    }

    @Test
    public void testFindFilterInvalidAccessor() {
        insert();
        DocumentCursor cursor = collection.find(where("lastName.name").eq("ln2"));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testIssue144() {
        Document doc1 = createDocument().put("id", "test-1").put("fruit", "Apple");
        Document doc2 = createDocument().put("id", "test-2").put("fruit", "Ôrange");
        Document doc3 = createDocument().put("id", "test-3").put("fruit", "Pineapple");

        NitriteCollection coll = db.getCollection("test");
        coll.insert(doc1, doc2, doc3);

        DocumentCursor cursor = coll.find().sort("fruit", SortOrder.Ascending,
            Collator.getInstance(Locale.FRANCE));
        assertEquals(cursor.toList().get(1).get("fruit"), "Ôrange");
    }

    @Test
    public void testIdSet() {
        insert();
        DocumentCursor cursor = collection.find(where("lastName").eq("ln2"));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("lastName").eq("ln1"));
        assertEquals(cursor.size(), 1);

        Document byId = cursor.iterator().next();
        assertEquals(byId.get("lastName"), "ln1");
    }

    @Test
    public void testCollectionField() {
        Document document = createDocument("name", "John")
            .put("tags", new Document[]{
                createDocument("type", "example").put("other", "value"),
                createDocument("type", "another-example").put("other", "some-other-value")
            });

        NitriteCollection example = db.getCollection("example");
        example.insert(document);

        document = createDocument("name", "Jane")
            .put("tags", new Document[]{
                createDocument("type", "example2").put("other", "value2"),
                createDocument("type", "another-example2").put("other", "some-other-value2")
            });
        example.insert(document);

        DocumentCursor cursor = example.find(where("tags").elemMatch(where("type").eq("example")));
        for (Document doc : cursor) {
            assertNotNull(doc);
            assertEquals(doc.get("name"), "John");
        }
    }

    @Test
    public void testBetweenFilter() {
        Document doc1 = createDocument("age", 31).put("tag", "one");
        Document doc2 = createDocument("age", 32).put("tag", "two");
        Document doc3 = createDocument("age", 33).put("tag", "three");
        Document doc4 = createDocument("age", 34).put("tag", "four");
        Document doc5 = createDocument("age", 35).put("tag", "five");

        NitriteCollection collection = db.getCollection("tag");
        collection.insert(doc1, doc2, doc3, doc4, doc5);
        collection.createIndex("age", IndexOptions.indexOptions(IndexType.Unique));

        DocumentCursor cursor = collection.find(where("age").between(31, 35));
        assertEquals(cursor.size(), 5);

        cursor = collection.find(where("age").between(31, 35, false));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(where("age").between(31, 35, false, true));
        assertEquals(cursor.size(), 4);

        cursor = collection.find(where("age").between(31, 35, false).not());
        assertEquals(cursor.size(), 2);
    }
}
