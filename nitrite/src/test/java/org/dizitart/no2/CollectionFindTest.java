package org.dizitart.no2;

import org.dizitart.no2.internals.JacksonMapper;
import org.dizitart.no2.internals.NitriteMapper;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.dizitart.no2.Constants.DOC_ID;
import static org.dizitart.no2.Constants.DOC_REVISION;
import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.FindOptions.limit;
import static org.dizitart.no2.FindOptions.sort;
import static org.dizitart.no2.filters.Filters.*;
import static org.dizitart.no2.util.Iterables.isSorted;
import static org.junit.Assert.*;

public class CollectionFindTest extends BaseCollectionTest {

    @Test
    public void testFindAll() {
        insert();

        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testFindWithFilter() throws ParseException {
        insert();

        Cursor cursor = collection.find(gt("birthDay",
                simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(gte("birthDay",
                simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(lt("birthDay",
                simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(lte("birthDay",
                simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(lte("birthDay",
                new Date()));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(lt("birthDay",
                new Date()));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(gt("birthDay",
                new Date()));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(gte("birthDay",
                new Date()));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(
                and(
                        lte("birthDay", new Date()),
                        eq("firstName", "fn1")
                ));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
                or(
                        lte("birthDay", new Date()),
                        eq("firstName", "fn12")
                ));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(
                and(
                        or(
                                lte("birthDay", new Date()),
                                eq("firstName", "fn12")
                        ),
                        eq("lastName", "ln1")
                ));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
                not(
                        and(
                                or(
                                        lte("birthDay", new Date()),
                                        eq("firstName", "fn12")
                                ),
                                eq("lastName", "ln1")
                        )
                ));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(eq("data.1", 4));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(lt("data.1", 4));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(in("lastName", "ln1", "ln2", "ln10"));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(not(in("firstName", "fn1", "fn2")));
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testFindWithLimit() {
        insert();

        Cursor cursor = collection.find(limit(0, 1));
        assertEquals(cursor.size(), 1);
        assertTrue(cursor.hasMore());

        cursor = collection.find(limit(1, 3));
        assertEquals(cursor.size(), 2);
        assertFalse(cursor.hasMore());

        cursor = collection.find(limit(0, 30));
        assertEquals(cursor.size(), 3);
        assertFalse(cursor.hasMore());

        cursor = collection.find(limit(2, 3));
        assertEquals(cursor.size(), 1);
        assertFalse(cursor.hasMore());
    }

    @Test
    public void testFindSortAscending() {
        insert();

        Cursor cursor = collection.find(sort("birthDay", SortOrder.Ascending));
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

        Cursor cursor = collection.find(sort("birthDay", SortOrder.Descending));
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

        Cursor cursor = collection.find(
                sort("birthDay", SortOrder.Descending).thenLimit(1, 2));
        assertEquals(cursor.size(), 2);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, false));

        cursor = collection.find(
                sort("birthDay", SortOrder.Ascending).thenLimit(1, 2));
        assertEquals(cursor.size(), 2);
        dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, true));

        cursor = collection.find(
                sort("firstName", SortOrder.Ascending).thenLimit(0, 30));
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
        Cursor cursor = collection.find(sort("my-value", SortOrder.Descending));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testFindInvalidField(){
        insert();
        Cursor cursor = collection.find(eq("myField", "myData"));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testFindInvalidFieldWithInvalidAccessor() {
        insert();
        Cursor cursor = collection.find(eq("myField.0", "myData"));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testFindLimitAndSortInvalidField() {
        insert();
        Cursor cursor = collection.find(
                sort("birthDay2", SortOrder.Descending).thenLimit(1, 2));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testGetById() {
        collection.insert(doc1);
        NitriteId id = NitriteId.createId(1L);
        Document document = collection.getById(id);
        assertNull(document);

        document = collection.find().firstOrDefault();

        assertEquals(document.get(DOC_ID), document.getId().getIdValue());
        assertEquals(document.get("firstName"), "fn1");
        assertEquals(document.get("lastName"), "ln1");
        assertArrayEquals((byte[]) document.get("data"), new byte[] {1, 2, 3});
        assertEquals(document.get("body"), "a quick brown fox jump over the lazy dog");
    }

    @Test
    public void testFindWithFilterAndOption() {
        insert();
        Cursor cursor = collection.find(lte("birthDay", new Date()),
                sort("firstName", SortOrder.Ascending).thenLimit(1, 2));
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testFindTextWithRegex() {
        insert();
        Cursor cursor = collection.find(regex("body", "hello"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(regex("body", "test"));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(regex("body", "^hello$"));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(regex("body", ".*"));
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testProject() {
        insert();
        Cursor cursor = collection.find(lte("birthDay", new Date()),
                sort("firstName", SortOrder.Ascending).thenLimit(0, 3));
        int iteration = 0;
        for (Document document : cursor) {
            switch (iteration) {
                case 0:
                    assertEquals(document, doc1);
                    break;
                case 1:
                    assertEquals(document, doc2);
                    break;
                case 2:
                    assertEquals(document, doc3);
                    break;
            }
            iteration++;
        }
        assertEquals(iteration, 3);
    }

    @Test
    public void testProjectWithCustomDocument() {
        insert();
        Cursor cursor = collection.find(lte("birthDay", new Date()),
                sort("firstName", SortOrder.Ascending).thenLimit(0, 3));

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
    public void testSingleProjection() {
        insert();
        Cursor cursor = collection.find(lte("birthDay", new Date()),
                sort("firstName", SortOrder.Ascending).thenLimit(0, 3));

        Document projection = createDocument("firstName", null)
                .put("lastName", "ln2");

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
                    assertEquals(document.get("firstName"), "fn2");
                    assertEquals(document.get("lastName"), "ln2");
                    break;
                case 1:
                    assertEquals(document.get("firstName"), "fn3");
                    assertEquals(document.get("lastName"), "ln2");
                    break;
            }
            iteration++;
        }
        assertEquals(iteration, 2);
    }

    @Test
    public void testFindWithArrayEqual() {
        insert();
        Cursor ids = collection.find(eq("data", new Object[]{3, 4, 3}));
        assertNotNull(ids);
        assertEquals(ids.size(), 1);
    }

    @Test
    public void testFindWithArrayEqualFailForWrongCardinality() {
        insert();
        Cursor ids = collection.find(eq("data", new byte[]{4, 3, 3}));
        assertNotNull(ids);
        assertEquals(ids.size(), 0);
    }

    @Test
    public void testFindWithIterableEqual() {
        insert();
        Cursor ids = collection.find(eq("list",
                new ArrayList<String>() {{ add("three"); add("four"); add("three"); }}));
        assertNotNull(ids);
        assertEquals(ids.size(), 1);
    }

    @Test
    public void testFindWithIterableEqualFailForWrongCardinality() {
        insert();
        Cursor ids = collection.find(eq("list",
                new ArrayList<String>() {{ add("four"); add("three"); add("three"); }}));
        assertNotNull(ids);
        assertEquals(ids.size(), 0);
    }

    @Test
    public void testFindInArray() {
        insert();
        Cursor ids = collection.find(elemMatch("data", and(gte("$", 2), lt("$", 5))));
        assertNotNull(ids);
        assertEquals(ids.size(), 3);

        ids = collection.find(elemMatch("data", or(gt("$", 2), lte("$", 5))));
        assertNotNull(ids);
        assertEquals(ids.size(), 3);

        ids = collection.find(elemMatch("data", and(gt("$", 1), lt("$", 4))));
        assertNotNull(ids);
        assertEquals(ids.size(), 2);
    }

    @Test
    public void testFindInList() {
        insert();
        Cursor ids = collection.find(elemMatch("list", regex("$", "three")));
        assertNotNull(ids);
        assertEquals(ids.size(), 2);

        ids = collection.find(elemMatch("list", regex("$", "hello")));
        assertNotNull(ids);
        assertEquals(ids.size(), 0);

        ids = collection.find(elemMatch("list", not(regex("$", "hello"))));
        assertNotNull(ids);
        assertEquals(ids.size(), 2);
    }

    @Test
    public void testElemMatchFilter() throws IOException {
        NitriteMapper parser = new JacksonMapper();
        Document doc1 = parser.parse("{ productScores: [ { product: \"abc\", score: 10 }, " +
                "{ product: \"xyz\", score: 5 } ], strArray: [\"a\", \"b\"]}");
        Document doc2 = parser.parse("{ productScores: [ { product: \"abc\", score: 8 }, " +
                "{ product: \"xyz\", score: 7 } ], strArray: [\"d\", \"e\"] }");
        Document doc3 = parser.parse("{ productScores: [ { product: \"abc\", score: 7 }, " +
                "{ product: \"xyz\", score: 8 } ], strArray: [\"a\", \"f\"] }");

        NitriteCollection prodCollection = db.getCollection("prodScore");
        prodCollection.insert(doc1, doc2, doc3);

        List<Document> documentList = prodCollection.find(elemMatch("productScores",
                and(eq("product", "xyz"), gte("score", 8)))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(elemMatch("productScores",
                not(lte("score", 8)))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(elemMatch("productScores",
                or(eq("product", "xyz"), gte("score", 8)))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(elemMatch("productScores",
                (eq("product", "xyz")))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(elemMatch("productScores",
                (gte("score", 10)))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(elemMatch("productScores",
                (gt("score", 8)))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(elemMatch("productScores",
                (lt("score", 7)))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(elemMatch("productScores",
                (lte("score", 7)))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(elemMatch("productScores",
                (in("score", 7, 8)))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(elemMatch("productScores",
                (regex("product", "xyz")))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(elemMatch("strArray",
                eq("$", "a"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(elemMatch("strArray",
                not(or(eq("$", "a"),
                        eq("$", "f"),
                        eq("$", "b"))))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(elemMatch("strArray",
                gt("$", "e"))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(elemMatch("strArray",
                gte("$", "e"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(elemMatch("strArray",
                lte("$", "b"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(elemMatch("strArray",
                lt("$", "a"))).toList();
        assertEquals(documentList.size(), 0);

        documentList = prodCollection.find(elemMatch("strArray",
                in("$", "a", "f"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(elemMatch("strArray",
                regex("$", "a"))).toList();
        assertEquals(documentList.size(), 2);

    }

    @Test
    public void testNotEqualFilter() {
        Document document = createDocument("abc", "123");
        document.put("xyz", null);

        collection.insert(document);
        Cursor cursor = collection.find(eq("abc", "123"));
        assertEquals(cursor.size(), 1);
        assertEquals(cursor.toList().size(), 1);

        cursor = collection.find(eq("xyz", null));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(not(eq("abc", null)));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(and(
                not(eq("abc", null)),
                eq("xyz", null)
        ));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(and(
                eq("abc", null),
                not(eq("xyz", null))
        ));
        assertEquals(cursor.size(), 0);

        collection.remove(ALL);

        document = createDocument("field", "two");
        document.put(DOC_REVISION, 1482225343161L);

        collection.insert(document);
        Document projection = collection.find(
                and(
                        gte(DOC_REVISION, 1482225343160L),
                        lte(DOC_REVISION, 1482225343162L),
                        not(eq(DOC_REVISION, null))
                )
        ).firstOrDefault();

        assertNull(projection);
    }
}
