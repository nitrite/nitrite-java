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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionFindBySingleFieldIndexTest extends BaseCollectionTest {

    @Test
    public void testFindByUniqueIndex() throws ParseException {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.Unique), "firstName");
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("firstName").eq("fn10"));
        assertEquals(cursor.size(), 0);

        collection.createIndex(IndexOptions.indexOptions(IndexType.Unique), "birthDay");
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
        collection.createIndex(IndexOptions.indexOptions(IndexType.NonUnique), "lastName");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NonUnique), "birthDay");

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
        collection.createIndex(IndexOptions.indexOptions(IndexType.Fulltext), "body");
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
        collection.createIndex(IndexOptions.indexOptions(IndexType.Fulltext), "body");
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
        collection.createIndex(IndexOptions.indexOptions(IndexType.Unique), "birthDay");

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
        collection.createIndex(IndexOptions.indexOptions(IndexType.Unique), "birthDay");

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
        collection.createIndex(IndexOptions.indexOptions(IndexType.Unique), "birthDay");

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
        collection.createIndex(IndexOptions.indexOptions(IndexType.Unique), "firstName");
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);

        collection.dropIndex("firstName");
        cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testFindTextWithWildCard() {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.Fulltext), "body");

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
        collection.createIndex(IndexOptions.indexOptions(IndexType.Fulltext), "body");

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

        collection.createIndex(IndexOptions.indexOptions(IndexType.Unique), "firstName");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NonUnique), "lastName");

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

        collection.createIndex(IndexOptions.indexOptions(IndexType.Fulltext), "notes");
        collection.insert(doc1, doc2, doc3, doc4);

        DocumentCursor cursor = collection.find(where("notes").text("fox"));
        assertEquals(cursor.size(), 4);

        cursor = collection.find(where("notes").text("dog"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("notes").text("lazy"));
        assertEquals(cursor.size(), 3);
    }
}
