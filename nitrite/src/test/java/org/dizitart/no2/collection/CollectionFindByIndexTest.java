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

package org.dizitart.no2.collection;

import org.dizitart.no2.BaseCollectionTest;
import org.dizitart.no2.Document;
import org.dizitart.no2.exceptions.FilterException;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.dizitart.no2.collection.FindOptions.sort;
import static org.dizitart.no2.filters.Filters.*;
import static org.dizitart.no2.util.Iterables.isSorted;
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
        Cursor cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(eq("firstName", "fn10"));
        assertEquals(cursor.size(), 0);

        collection.createIndex("birthDay", IndexOptions.indexOptions(IndexType.Unique));
        cursor = collection.find(gt("birthDay",
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
    public void testFindByNonUniqueIndex() throws ParseException {
        insert();
        collection.createIndex("lastName", IndexOptions.indexOptions(IndexType.NonUnique));
        collection.createIndex("birthDay", IndexOptions.indexOptions(IndexType.NonUnique));

        Cursor cursor = collection.find(eq("lastName", "ln2"));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(eq("lastName", "ln20"));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(gt("birthDay",
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
    public void testFindByFullTextIndexAfterInsert() {
        insert();
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));
        assertTrue(collection.hasIndex("body"));

        Cursor cursor = collection.find(text("body", "Lorem"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(text("body", "nosql"));
        assertEquals(cursor.size(), 0);

        collection.dropIndex("body");
        boolean filterException = false;
        try {
            collection.find(text("body", "Lorem"));
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

        Cursor cursor = collection.find(text("body", "Lorem"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(text("body", "quick brown"));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(text("body", "nosql"));
        assertEquals(cursor.size(), 0);

        collection.dropIndex("body");
        boolean filterException = false;
        try {
            collection.find(text("body", "Lorem"));
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

        Cursor cursor = collection.find(sort("birthDay", SortOrder.Ascending));
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

        Cursor cursor = collection.find(sort("birthDay", SortOrder.Descending));
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
    public void testFindAfterDroppedIndex() {
        insert();
        collection.createIndex("firstName", IndexOptions.indexOptions(IndexType.Unique));
        Cursor cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 1);

        collection.dropIndex("firstName");
        cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testFindTextWithWildCard() {
        insert();
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));

        Cursor cursor = collection.find(text("body", "Lo"));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(text("body", "Lo*"));
        assertEquals(cursor.size(), 1);      // Lorem

        cursor = collection.find(text("body", "*rem"));
        assertEquals(cursor.size(), 1);      // lorem

        cursor = collection.find(text("body", "*or*"));
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testFindTextWithEmptyString() {
        insert();
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));

        Cursor cursor = collection.find(text("body", ""));
        assertEquals(cursor.size(), 0);
    }

}
