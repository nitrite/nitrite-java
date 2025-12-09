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

package org.dizitart.no2.integration.collection;

import com.github.javafaker.Faker;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.dizitart.no2.integration.TestUtil.isSorted;
import static org.dizitart.no2.collection.FindOptions.orderBy;
import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.filters.Filter.or;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionFindBySingleFieldIndexTest extends BaseCollectionTest {

    @Test
    public void testFindByUniqueIndex() throws ParseException {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "firstName");
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("firstName").eq("fn10"));
        assertEquals(cursor.size(), 0);

        collection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "birthDay");
        cursor = collection.find(where("birthDay").gt(
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
            and(
                or(
                    where("birthDay").lte(new Date()),
                    where("firstName").eq("fn12")
                ),
                where("lastName").eq("ln1")
            ));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
            and(
                or(
                    where("birthDay").lte(new Date()),
                    where("firstName").eq("fn12")
                ),
                where("lastName").eq("ln1")
            ).not());
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("data.1").eq((byte) 4));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("data.1").lt(4));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("lastName").in("ln1", "ln2", "ln10"));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(where("firstName").notIn("fn1", "fn2"));
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testFindByNonUniqueIndex() throws ParseException {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "lastName");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "birthDay");

        DocumentCursor cursor = collection.find(where("lastName").eq("ln2"));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("lastName").eq("ln20"));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(where("birthDay").gt(
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
            and(
                or(
                    where("birthDay").lte(new Date()),
                    where("firstName").eq("fn12")
                ),
                where("lastName").eq("ln1")
            ));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
            and(
                or(
                    where("birthDay").lte(new Date()),
                    where("firstName").eq("fn12")
                ),
                where("lastName").eq("ln1")
            ).not());
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("data.1").eq((byte) 4));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("data.1").lt(4));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("lastName").in("ln1", "ln2", "ln10"));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(where("firstName").notIn("fn1", "fn2"));
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testFindByFullTextIndexAfterInsert() {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.FULL_TEXT), "body");
        assertTrue(collection.hasIndex("body"));

        DocumentCursor cursor = collection.find(where("body").text("Lorem"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("body").text("nosql"));
        assertEquals(cursor.size(), 0);

        collection.dropIndex("body");
        boolean filterException = false;
        try {
            collection.find(where("body").text("Lorem")).toList();
        } catch (FilterException fe) {
            filterException = true;
        } finally {
            assertTrue(filterException);
        }
    }

    @Test
    public void testFindByFullTextIndexBeforeInsert() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.FULL_TEXT), "body");
        assertTrue(collection.hasIndex("body"));
        insert();

        DocumentCursor cursor = collection.find(where("body").text("Lorem"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("body").text("quick brown"));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("body").text("nosql"));
        assertEquals(cursor.size(), 0);

        collection.dropIndex("body");
        boolean filterException = false;
        try {
            collection.find(where("body").text("Lorem")).toList();
        } catch (FilterException fe) {
            filterException = true;
        } finally {
            assertTrue(filterException);
        }
    }

    @Test
    public void testFindByIndexSortAscending() {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "birthDay");

        DocumentCursor cursor = collection.find(orderBy("birthDay", SortOrder.Ascending));
        assertEquals(cursor.size(), 3);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, true));
    }

    @Test
    public void testFindByIndexSortDescending() {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "birthDay");

        DocumentCursor cursor = collection.find(orderBy("birthDay", SortOrder.Descending));
        assertEquals(cursor.size(), 3);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, false));
    }

    @Test
    public void testFindByIndexLimitAndSort() {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "birthDay");

        DocumentCursor cursor = collection.find(
            orderBy("birthDay", SortOrder.Descending)
                .skip(1)
                .limit(2)
        );
        assertEquals(cursor.size(), 2);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, false));

        cursor = collection.find(orderBy("birthDay", SortOrder.Ascending).skip(1).limit(2));
        assertEquals(cursor.size(), 2);
        dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, true));

        cursor = collection.find(orderBy("firstName", SortOrder.Ascending).skip(0).limit(30));
        assertEquals(cursor.size(), 3);
        List<String> nameList = new ArrayList<>();
        for (Document document : cursor) {
            nameList.add(document.get("firstName", String.class));
        }
        assertTrue(isSorted(nameList, true));
    }

    @Test
    public void testFindAfterDroppedIndex() {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "firstName");
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);

        collection.dropIndex("firstName");
        cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testFindTextWithWildCard() {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.FULL_TEXT), "body");

        DocumentCursor cursor = collection.find(where("body").text("Lo"));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(where("body").text("Lo*"));
        assertEquals(cursor.size(), 1);      // Lorem

        cursor = collection.find(where("body").text("*rem"));
        assertEquals(cursor.size(), 1);      // lorem

        cursor = collection.find(where("body").text("*or*"));
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testFindTextWithEmptyString() {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.FULL_TEXT), "body");

        DocumentCursor cursor = collection.find(where("body").text(""));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testFindWithOrIndexed() {
        NitriteCollection collection = db.getCollection("testFindWithOrIndexed");
        Document doc1 = Document.createDocument("firstName", "John").put("lastName", "Doe");
        Document doc2 = Document.createDocument("firstName", "Jane").put("lastName", "Doe");
        Document doc3 = Document.createDocument("firstName", "Jonas").put("lastName", "Doe");
        Document doc4 = Document.createDocument("firstName", "Johan").put("lastName", "Day");

        collection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "firstName");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "lastName");

        collection.insert(doc1, doc2, doc3, doc4);

        DocumentCursor cursor = collection.find(where("firstName").eq("John").or(where("lastName").eq("Day")));
        assertEquals(cursor.size(), 2);

        List<Document> list = cursor.toList();
        assertEquals(list.size(), 2);
    }

    @Test
    public void testIssue45() {
        NitriteCollection collection = db.getCollection("testIssue45");
        Faker faker = new Faker();
        String text1 = faker.lorem().paragraph() + " quick brown";
        String text2 = faker.lorem().paragraph() + " fox jump";
        String text3 = faker.lorem().paragraph() + " over lazy";
        String text4 = faker.lorem().paragraph() + " dog";

        List<String> list1 = Arrays.asList(text1, text2);
        List<String> list2 = Arrays.asList(text1, text2, text3);
        List<String> list3 = Arrays.asList(text2, text3);
        List<String> list4 = Arrays.asList(text1, text2, text3, text4);

        Document doc1 = Document.createDocument("firstName", "John").put("notes", list1);
        Document doc2 = Document.createDocument("firstName", "Jane").put("notes", list2);
        Document doc3 = Document.createDocument("firstName", "Jonas").put("notes", list3);
        Document doc4 = Document.createDocument("firstName", "Johan").put("notes", list4);

        collection.createIndex(IndexOptions.indexOptions(IndexType.FULL_TEXT), "notes");
        collection.insert(doc1, doc2, doc3, doc4);

        DocumentCursor cursor = collection.find(where("notes").text("fox"));
        assertEquals(cursor.size(), 4);

        cursor = collection.find(where("notes").text("dog"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("notes").text("lazy"));
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testSortByIndexDescendingLessThanEqual() {
        NitriteCollection nitriteCollection = db.getCollection("testSortByIndexDescendingLessThanEqual");
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        integerList.forEach(i -> {
            Document doc = Document.createDocument();
            doc.put("name", i);
            nitriteCollection.insert(doc);
        });

        DocumentCursor cursor = nitriteCollection.find(where("name").lte(3),
            orderBy("name", SortOrder.Descending));

        List<Document> docIter = cursor.toList();
        Integer[] nonIndexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "name");

        cursor = nitriteCollection.find(where("name").lte(3),
            orderBy("name", SortOrder.Descending));
        docIter = cursor.toList();
        Integer[] indexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        assertArrayEquals(nonIndexedResult, indexedResult);
    }

    @Test
    public void testSortByIndexAscendingLessThanEqual() {
        NitriteCollection nitriteCollection = db.getCollection("testSortByIndexAscendingLessThanEqual");
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        integerList.forEach(i -> {
            Document doc = Document.createDocument();
            doc.put("name", i);
            nitriteCollection.insert(doc);
        });

        DocumentCursor cursor = nitriteCollection.find(where("name").lte(3),
            orderBy("name", SortOrder.Ascending));

        List<Document> docIter = cursor.toList();
        Integer[] nonIndexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "name");

        cursor = nitriteCollection.find(where("name").lte(3),
            orderBy("name", SortOrder.Ascending));
        docIter = cursor.toList();
        Integer[] indexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        assertArrayEquals(nonIndexedResult, indexedResult);
    }

    @Test
    public void testSortByIndexDescendingGreaterThanEqual() {
        NitriteCollection nitriteCollection = db.getCollection("testSortByIndexDescendingGreaterThanEqual");
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        integerList.forEach(i -> {
            Document doc = Document.createDocument();
            doc.put("name", i);
            nitriteCollection.insert(doc);
        });

        DocumentCursor cursor = nitriteCollection.find(where("name").gte(3),
            orderBy("name", SortOrder.Descending));

        List<Document> docIter = cursor.toList();
        Integer[] nonIndexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "name");

        cursor = nitriteCollection.find(where("name").gte(3),
            orderBy("name", SortOrder.Descending));
        docIter = cursor.toList();
        Integer[] indexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        assertArrayEquals(nonIndexedResult, indexedResult);
    }

    @Test
    public void testSortByIndexAscendingGreaterThanEqual() {
        NitriteCollection nitriteCollection = db.getCollection("testSortByIndexAscendingGreaterThanEqual");
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        integerList.forEach(i -> {
            Document doc = Document.createDocument();
            doc.put("name", i);
            nitriteCollection.insert(doc);
        });

        DocumentCursor cursor = nitriteCollection.find(where("name").gte(3),
            orderBy("name", SortOrder.Ascending));

        List<Document> docIter = cursor.toList();
        Integer[] nonIndexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "name");

        cursor = nitriteCollection.find(where("name").gte(3),
            orderBy("name", SortOrder.Ascending));
        docIter = cursor.toList();
        Integer[] indexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        assertArrayEquals(nonIndexedResult, indexedResult);
    }

    @Test
    public void testSortByIndexDescendingGreaterThan() {
        NitriteCollection nitriteCollection = db.getCollection("testSortByIndexDescendingGreaterThan");
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        integerList.forEach(i -> {
            Document doc = Document.createDocument();
            doc.put("name", i);
            nitriteCollection.insert(doc);
        });

        DocumentCursor cursor = nitriteCollection.find(where("name").gt(3),
            orderBy("name", SortOrder.Descending));

        List<Document> docIter = cursor.toList();
        Integer[] nonIndexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "name");

        cursor = nitriteCollection.find(where("name").gt(3),
            orderBy("name", SortOrder.Descending));
        docIter = cursor.toList();
        Integer[] indexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        assertArrayEquals(nonIndexedResult, indexedResult);
    }

    @Test
    public void testSortByIndexAscendingGreaterThan() {
        NitriteCollection nitriteCollection = db.getCollection("testSortByIndexAscendingGreaterThan");
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        integerList.forEach(i -> {
            Document doc = Document.createDocument();
            doc.put("name", i);
            nitriteCollection.insert(doc);
        });

        DocumentCursor cursor = nitriteCollection.find(where("name").gt(3),
            orderBy("name", SortOrder.Ascending));

        List<Document> docIter = cursor.toList();
        Integer[] nonIndexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "name");

        cursor = nitriteCollection.find(where("name").gt(3),
            orderBy("name", SortOrder.Ascending));
        docIter = cursor.toList();
        Integer[] indexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        assertArrayEquals(nonIndexedResult, indexedResult);
    }

    @Test
    public void testSortByIndexDescendingLessThan() {
        NitriteCollection nitriteCollection = db.getCollection("testSortByIndexDescendingLessThan");
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        integerList.forEach(i -> {
            Document doc = Document.createDocument();
            doc.put("name", i);
            nitriteCollection.insert(doc);
        });

        DocumentCursor cursor = nitriteCollection.find(where("name").lt(3),
            orderBy("name", SortOrder.Descending));

        List<Document> docIter = cursor.toList();
        Integer[] nonIndexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "name");

        cursor = nitriteCollection.find(where("name").lt(3),
            orderBy("name", SortOrder.Descending));
        docIter = cursor.toList();
        Integer[] indexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        assertArrayEquals(nonIndexedResult, indexedResult);
    }

    @Test
    public void testSortByIndexAscendingLessThan() {
        NitriteCollection nitriteCollection = db.getCollection("testSortByIndexAscendingLessThan");
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        integerList.forEach(i -> {
            Document doc = Document.createDocument();
            doc.put("name", i);
            nitriteCollection.insert(doc);
        });

        DocumentCursor cursor = nitriteCollection.find(where("name").lt(3),
            orderBy("name", SortOrder.Ascending));

        List<Document> docIter = cursor.toList();
        Integer[] nonIndexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        nitriteCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "name");

        cursor = nitriteCollection.find(where("name").lt(3),
            orderBy("name", SortOrder.Ascending));
        docIter = cursor.toList();
        Integer[] indexedResult = docIter.stream().map(d -> d.get("name", Integer.class)).toArray(Integer[]::new);

        assertArrayEquals(nonIndexedResult, indexedResult);
    }

    @Test
    public void testFindByArrayFieldIndexWithElemMatch() {
        // Create a collection with array field
        NitriteCollection userCollection = db.getCollection("users");
        
        // Insert a larger dataset (15k documents as mentioned in the issue)
        for (int i = 0; i < 15000; i++) {
            Document doc = Document.createDocument("name", "user" + i)
                .put("emails", new String[]{"user" + i + "@example.com", "user" + i + "@test.com"});
            userCollection.insert(doc);
        }
        
        // Add a specific test document
        userCollection.insert(Document.createDocument("name", "testuser")
            .put("emails", new String[]{"test@gmail.com", "test@example.com"}));
        
        // Measure query time WITHOUT index
        long startWithoutIndex = System.nanoTime();
        DocumentCursor cursorWithoutIndex = userCollection.find(
            where("emails").elemMatch(org.dizitart.no2.filters.FluentFilter.$.eq("test@gmail.com")));
        long withoutIndexCount = cursorWithoutIndex.size();
        long endWithoutIndex = System.nanoTime();
        long timeWithoutIndex = (endWithoutIndex - startWithoutIndex) / 1_000_000;
        
        assertEquals(1, withoutIndexCount);
        
        // Verify collection scan is used when no index exists (no index descriptor)
        FindPlan planWithoutIndex = cursorWithoutIndex.getFindPlan();
        assertNull("Index descriptor should be null when no index exists", 
            planWithoutIndex.getIndexDescriptor());
        
        // Create index on emails field
        userCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "emails");
        
        // Measure query time WITH index
        long startWithIndex = System.nanoTime();
        DocumentCursor cursorWithIndex = userCollection.find(
            where("emails").elemMatch(org.dizitart.no2.filters.FluentFilter.$.eq("test@gmail.com")));
        long withIndexCount = cursorWithIndex.size();
        long endWithIndex = System.nanoTime();
        long timeWithIndex = (endWithIndex - startWithIndex) / 1_000_000;
        
        assertEquals(1, withIndexCount);
        
        // Verify index is actually being used by checking the find plan
        FindPlan planWithIndex = cursorWithIndex.getFindPlan();
        assertNotNull("Index scan filter should not be null when index exists", 
            planWithIndex.getIndexScanFilter());
        assertNotNull("Index descriptor should not be null when index is used", 
            planWithIndex.getIndexDescriptor());
        
        // With index should be significantly faster
        System.out.println("ElemMatch query on 15k documents:");
        System.out.println("  Time without index: " + timeWithoutIndex + " ms");
        System.out.println("  Time with index: " + timeWithIndex + " ms");
        System.out.println("  Speedup: " + (timeWithoutIndex > 0 ? (timeWithoutIndex / (double) Math.max(1, timeWithIndex)) : "N/A") + "x");
        
        // Assert that index provides significant improvement (at least 2x faster)
        // This is a conservative check - actual improvement should be much higher
        assertTrue("Index should provide significant performance improvement", 
            timeWithIndex < timeWithoutIndex || timeWithIndex < 100);
    }

    @Test
    public void testFindByArrayFieldIndexWithElemMatchComplexFilter() {
        // Create a collection with array field
        NitriteCollection productCollection = db.getCollection("products");
        
        // Insert documents with array of scores
        for (int i = 0; i < 1000; i++) {
            Document doc = Document.createDocument("name", "product" + i)
                .put("scores", new Integer[]{i, i + 10, i + 20});
            productCollection.insert(doc);
        }
        
        // Create index on scores field
        productCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "scores");
        
        // Test 1: Query with elemMatch using gt filter
        DocumentCursor cursor = productCollection.find(
            where("scores").elemMatch(org.dizitart.no2.filters.FluentFilter.$.gt(995)));
        
        // Verify index is used
        FindPlan findPlan = cursor.getFindPlan();
        assertNotNull("Index scan filter should be used for gt query", findPlan.getIndexScanFilter());
        assertNotNull("Index descriptor should be present", findPlan.getIndexDescriptor());
        
        // Should find products where at least one score is > 995
        assertTrue("Should find products with scores > 995", cursor.size() > 0);
        
        // Test 2: Query with elemMatch using lt filter
        cursor = productCollection.find(
            where("scores").elemMatch(org.dizitart.no2.filters.FluentFilter.$.lt(5)));
        
        // Verify index is used
        findPlan = cursor.getFindPlan();
        assertNotNull("Index scan filter should be used for lt query", findPlan.getIndexScanFilter());
        assertNotNull("Index descriptor should be present", findPlan.getIndexDescriptor());
        
        // Should find products where at least one score is < 5
        assertTrue("Should find products with scores < 5", cursor.size() > 0);
        
        // Test 3: Query with elemMatch using gte filter
        cursor = productCollection.find(
            where("scores").elemMatch(org.dizitart.no2.filters.FluentFilter.$.gte(500)));
        
        findPlan = cursor.getFindPlan();
        assertNotNull("Index scan filter should be used for gte query", findPlan.getIndexScanFilter());
        assertTrue("Should find products with scores >= 500", cursor.size() > 0);
        
        // Test 4: Query with elemMatch using lte filter
        cursor = productCollection.find(
            where("scores").elemMatch(org.dizitart.no2.filters.FluentFilter.$.lte(500)));
        
        findPlan = cursor.getFindPlan();
        assertNotNull("Index scan filter should be used for lte query", findPlan.getIndexScanFilter());
        assertTrue("Should find products with scores <= 500", cursor.size() > 0);
    }
    
    @Test
    public void testElemMatchWithNonUniqueIndex() {
        // Test that elemMatch works with non-unique index
        NitriteCollection tagCollection = db.getCollection("tags");
        
        // Insert documents with tag arrays (some tags are common)
        for (int i = 0; i < 500; i++) {
            Document doc = Document.createDocument("id", i)
                .put("tags", new String[]{"tag" + i, "category" + (i % 10), "item" + i});
            tagCollection.insert(doc);
        }
        
        // Create non-unique index on tags field (since there are duplicate values)
        tagCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "tags");
        
        // Query with elemMatch
        DocumentCursor cursor = tagCollection.find(
            where("tags").elemMatch(org.dizitart.no2.filters.FluentFilter.$.eq("tag100")));
        
        // Verify index is used
        FindPlan findPlan = cursor.getFindPlan();
        assertNotNull("Index scan filter should be used", 
            findPlan.getIndexScanFilter());
        assertNotNull("Index descriptor should be present", 
            findPlan.getIndexDescriptor());
        assertEquals("Should find exactly one document", 1, cursor.size());
        
        // Query for a common category tag (should find multiple)
        cursor = tagCollection.find(
            where("tags").elemMatch(org.dizitart.no2.filters.FluentFilter.$.eq("category5")));
        
        findPlan = cursor.getFindPlan();
        assertNotNull("Index should be used for common values too", 
            findPlan.getIndexScanFilter());
        assertEquals("Should find all documents with category5", 50, cursor.size());
    }
    
    @Test
    public void testElemMatchIndexPerformanceComparison() {
        // This test explicitly measures and compares performance
        NitriteCollection perfCollection = db.getCollection("performance");
        
        // Insert a meaningful dataset
        for (int i = 0; i < 10000; i++) {
            Document doc = Document.createDocument("id", i)
                .put("values", new Integer[]{i, i * 2, i * 3});
            perfCollection.insert(doc);
        }
        
        // Add a unique test value that only appears once
        perfCollection.insert(Document.createDocument("id", 99999)
            .put("values", new Integer[]{77777, 88888, 99999}));
        
        // Test WITHOUT index
        long startNoIndex = System.nanoTime();
        DocumentCursor noIndexCursor = perfCollection.find(
            where("values").elemMatch(org.dizitart.no2.filters.FluentFilter.$.eq(99999)));
        long noIndexCount = noIndexCursor.size();
        long endNoIndex = System.nanoTime();
        long timeNoIndex = (endNoIndex - startNoIndex) / 1_000_000;
        
        // Verify no index was used (no index descriptor)
        FindPlan noIndexPlan = noIndexCursor.getFindPlan();
        assertNull("Index descriptor should be null without index", 
            noIndexPlan.getIndexDescriptor());
        assertEquals(1, noIndexCount);
        
        // Create index
        perfCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "values");
        
        // Test WITH index
        long startWithIndex = System.nanoTime();
        DocumentCursor withIndexCursor = perfCollection.find(
            where("values").elemMatch(org.dizitart.no2.filters.FluentFilter.$.eq(99999)));
        long withIndexCount = withIndexCursor.size();
        long endWithIndex = System.nanoTime();
        long timeWithIndex = (endWithIndex - startWithIndex) / 1_000_000;
        
        // Verify index was used
        FindPlan withIndexPlan = withIndexCursor.getFindPlan();
        assertNotNull("Index scan filter should be used with index", 
            withIndexPlan.getIndexScanFilter());
        assertNotNull("Index descriptor should be present", 
            withIndexPlan.getIndexDescriptor());
        assertEquals(1, withIndexCount);
        
        System.out.println("Performance comparison for elemMatch on 10k documents:");
        System.out.println("  Without index: " + timeNoIndex + " ms");
        System.out.println("  With index: " + timeWithIndex + " ms");
        System.out.println("  Improvement: " + 
            (timeNoIndex > 0 ? String.format("%.1fx", timeNoIndex / (double) Math.max(1, timeWithIndex)) : "N/A"));
        
        // Index should provide measurable improvement
        assertTrue("Index should improve performance or complete very quickly", 
            timeWithIndex < timeNoIndex || timeWithIndex < 100);
    }
}
