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
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.dizitart.no2.common.util.TestUtil.isSorted;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionFindByIndexTest extends BaseCollectionTest {

    @Test
    public void testFindByUniqueIndex() throws ParseException {
        insert();
        collection.createIndex("firstName", IndexOptions.indexOptions(IndexType.Unique));
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("firstName").eq("fn10"));
        assertEquals(cursor.size(), 0);

        collection.createIndex("birthDay", IndexOptions.indexOptions(IndexType.Unique));
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
    }

    @Test
    public void testFindByNonUniqueIndex() throws ParseException {
        insert();
        collection.createIndex("lastName", IndexOptions.indexOptions(IndexType.NonUnique));
        collection.createIndex("birthDay", IndexOptions.indexOptions(IndexType.NonUnique));

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
    }

    @Test
    public void testFindByFullTextIndexAfterInsert() {
        insert();
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));
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
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));
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
        collection.createIndex("birthDay", IndexOptions.indexOptions(IndexType.Unique));

        DocumentCursor cursor = collection.find().sort("birthDay", SortOrder.Ascending);
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
        collection.createIndex("birthDay", IndexOptions.indexOptions(IndexType.Unique));

        DocumentCursor cursor = collection.find().sort("birthDay", SortOrder.Descending);
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
        collection.createIndex("birthDay", IndexOptions.indexOptions(IndexType.Unique));

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
    public void testFindAfterDroppedIndex() {
        insert();
        collection.createIndex("firstName", IndexOptions.indexOptions(IndexType.Unique));
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);

        collection.dropIndex("firstName");
        cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testFindTextWithWildCard() {
        insert();
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));

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
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));

        DocumentCursor cursor = collection.find(where("body").text(""));
        assertEquals(cursor.size(), 0);
    }

}
