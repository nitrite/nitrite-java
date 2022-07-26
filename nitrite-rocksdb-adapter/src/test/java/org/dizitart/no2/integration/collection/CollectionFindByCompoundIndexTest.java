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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.AndFilter;
import org.dizitart.no2.filters.IndexScanFilter;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.collection.FindOptions.orderBy;
import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.filters.Filter.or;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class CollectionFindByCompoundIndexTest extends BaseCollectionTest {

    @Test
    public void testFindByAndFilter() {
        insert();
        collection.createIndex("list", "lastName", "firstName");
        DocumentCursor cursor = collection.find(and(
            where("lastName").eq("ln2"),
            where("firstName").notEq("fn1"),
            where("list").eq("four")
        ));

        FindPlan findPlan = cursor.getFindPlan();
        assertNull(findPlan.getCollectionScanFilter());
        assertNotNull(findPlan.getIndexDescriptor());
        assertEquals(findPlan.getIndexDescriptor(), collection.listIndices().toArray()[0]);

        IndexScanFilter indexScanFilter = findPlan.getIndexScanFilter();
        assertEquals(indexScanFilter.getFilters().get(0), where("list").eq("four"));
        assertEquals(indexScanFilter.getFilters().get(1), where("lastName").eq("ln2"));
        assertEquals(indexScanFilter.getFilters().get(2), where("firstName").notEq("fn1"));

        List<Document> documents = cursor.toList();
        assertEquals(documents.size(), 1);
        assertEquals(documents.get(0).get("body", String.class), "quick hello world from nitrite");
    }

    @Test
    public void testFindByOrFilterAndFilter() {
        insert();
        collection.createIndex("lastName", "firstName");
        DocumentCursor cursor = collection.find(
            or(
                and(
                    where("lastName").eq("ln2"),
                    where("firstName").notEq("fn1")
                ),
                and(
                    where("firstName").eq("fn3"),
                    where("lastName").eq("ln2")
                )
            ), FindOptions.withDistinct()
        );

        assertEquals(2, cursor.size());

        FindPlan findPlan = cursor.getFindPlan();
        assertNull(findPlan.getIndexScanFilter());
        assertNull(findPlan.getCollectionScanFilter());
        assertNotNull(findPlan.getSubPlans());

        assertEquals(2, findPlan.getSubPlans().size());
        assertNotNull(findPlan.getSubPlans().get(0).getIndexScanFilter());
        assertNotNull(findPlan.getSubPlans().get(1).getIndexScanFilter());

        assertEquals(1, cursor.toList().stream().filter(d ->
            d.get("firstName", String.class).equals("fn2")
                && d.get("lastName", String.class).equals("ln2")).count());

        assertEquals(1, cursor.toList().stream().filter(d ->
            d.get("firstName", String.class).equals("fn3")
                && d.get("lastName", String.class).equals("ln2")).count());
    }

    @Test
    public void testFindByAndFilterOrFilter() {
        insert();
        collection.createIndex("lastName", "firstName");
        DocumentCursor cursor = collection.find(
            and(
                or(
                    where("lastName").eq("ln2"),
                    where("firstName").notEq("fn1")
                ),
                or(
                    where("firstName").eq("fn3"),
                    where("lastName").eq("ln2")
                )
            )
        );

        assertEquals(2, cursor.size());

        FindPlan findPlan = cursor.getFindPlan();
        assertNull(findPlan.getIndexScanFilter());
        assertNotNull(findPlan.getCollectionScanFilter());
        assertTrue(findPlan.getSubPlans().isEmpty());

        assertEquals(and(
            or(
                where("lastName").eq("ln2"),
                where("firstName").notEq("fn1")
            ),
            or(
                where("firstName").eq("fn3"),
                where("lastName").eq("ln2")
            )
        ), findPlan.getCollectionScanFilter());
    }

    @Test
    public void testFindByAndFilterAndFilter() {
        insert();
        collection.createIndex("lastName", "firstName");
        DocumentCursor cursor = collection.find(
            and(
                and(
                    where("lastName").eq("ln2"),
                    where("firstName").notEq("fn1")
                ),
                and(
                    where("firstName").eq("fn3"),
                    where("lastName").eq("ln2")
                )
            )
        );

        assertEquals(1, cursor.size());
        FindPlan findPlan = cursor.getFindPlan();

        assertNotNull(findPlan.getIndexDescriptor());
        assertEquals(collection.listIndices().toArray()[0], findPlan.getIndexDescriptor());

        assertNotNull(findPlan.getIndexScanFilter());
        assertEquals(findPlan.getIndexScanFilter().getFilters(),
            ((AndFilter) and(
                where("lastName").eq("ln2"),
                where("firstName").notEq("fn1")
            )).getFilters());

        assertNotNull(findPlan.getCollectionScanFilter());
        assertEquals(where("firstName").eq("fn3"), findPlan.getCollectionScanFilter());

        assertTrue(findPlan.getSubPlans().isEmpty());

        assertEquals(cursor.toList().get(0).get("firstName", String.class), "fn3");
    }

    @Test
    public void testFindByOrFilter() throws ParseException {
        // all or clause has index
        insert();
        collection.createIndex("lastName", "firstName");
        collection.createIndex("firstName");
        collection.createIndex("birthDay");
        DocumentCursor cursor = collection.find(
            or(
                or(
                    where("lastName").eq("ln2"),
                    where("firstName").notEq("fn1")
                ),
                where("birthDay").eq(simpleDateFormat.parse("2012-07-01T16:02:48.440Z")),
                where("firstName").notEq("fn1")
            )
        );

        FindPlan findPlan = cursor.getFindPlan();
        assertEquals(3, findPlan.getSubPlans().size());
        assertEquals(5, cursor.size());

        // distinct
        cursor = collection.find(
            or(
                or(
                    where("lastName").eq("ln2"),
                    where("firstName").notEq("fn1")
                ),
                where("birthDay").eq(simpleDateFormat.parse("2012-07-01T16:02:48.440Z")),
                where("firstName").notEq("fn1")
            ), FindOptions.withDistinct()
        );

        findPlan = cursor.getFindPlan();
        assertEquals(3, findPlan.getSubPlans().size());
        assertEquals(3, cursor.size());
    }

    @Test
    public void testFindOrNoIndex() throws ParseException {
        // on of the or clause has no index
        insert();
        collection.createIndex("lastName", "firstName");
        collection.createIndex("firstName");
        DocumentCursor cursor = collection.find(
            or(
                or(
                    where("lastName").eq("ln2"),
                    where("firstName").notEq("fn1")
                ),
                where("birthDay").eq(simpleDateFormat.parse("2012-07-01T16:02:48.440Z")),
                where("firstName").notEq("fn1")
            )
        );

        FindPlan findPlan = cursor.getFindPlan();
        assertEquals(0, findPlan.getSubPlans().size());
        assertEquals(3, cursor.size());
    }

    @Test
    public void testFindAndNoIndex() throws ParseException {
        // index at second field
        insert();
        collection.createIndex("lastName", "firstName");
        DocumentCursor cursor = collection.find(
            and(
                where("firstName").notEq("fn1"),
                where("birthDay").eq(simpleDateFormat.parse("2012-07-01T16:02:48.440Z"))
            )
        );

        FindPlan findPlan = cursor.getFindPlan();
        assertNull(findPlan.getIndexScanFilter());
        assertNull(findPlan.getIndexDescriptor());

        assertNotNull(findPlan.getCollectionScanFilter());
        assertEquals(0, cursor.size());
    }

    @Test
    public void testSortByIndex() throws ParseException {
        // multiple field sort in both direction
        collection.createIndex("lastName", "birthDay");

        Document doc = createDocument("firstName", "fn4")
            .put("lastName", "ln3")
            .put("birthDay", simpleDateFormat.parse("2016-04-17T16:02:48.440Z"))
            .put("data", new byte[]{9, 4, 8})
            .put("body", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed nunc mi, mattis ullamcorper dignissim vitae, condimentum non lorem.");
        collection.insert(doc);
        collection.insert(doc3);
        collection.insert(doc1);
        collection.insert(doc2);

        DocumentCursor cursor = collection.find(
            and(
                where("lastName").notEq("ln1"),
                where("birthDay").notEq(simpleDateFormat.parse("2012-07-01T16:02:48.440Z"))
            ),
            orderBy("lastName", SortOrder.Ascending)
                .thenOrderBy("birthDay", SortOrder.Descending)
        );

        List<Document> documents = cursor.toList();
        assertEquals(3, documents.size());

        Document document = documents.get(0);
        assertEquals("ln2", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2014-04-17T16:02:48.440Z"), document.get("birthDay"));

        document = documents.get(1);
        assertEquals("ln2", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2010-06-12T16:02:48.440Z"), document.get("birthDay"));

        document = documents.get(2);
        assertEquals("ln3", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2016-04-17T16:02:48.440Z"), document.get("birthDay"));

        FindPlan findPlan = cursor.getFindPlan();
        assertTrue(findPlan.getBlockingSortOrder().isEmpty());
        assertNull(findPlan.getCollectionScanFilter());
        assertNotNull(findPlan.getIndexDescriptor());
        assertFalse(findPlan.getIndexScanOrder().get("lastName"));

        // reverse scan
        assertTrue(findPlan.getIndexScanOrder().get("birthDay"));
    }

    @Test
    public void testBlockingSort() throws ParseException {
        // multiple field sort in memory

        Document doc = createDocument("firstName", "fn4")
            .put("lastName", "ln3")
            .put("birthDay", simpleDateFormat.parse("2016-04-17T16:02:48.440Z"))
            .put("data", new byte[]{9, 4, 8})
            .put("body", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed nunc mi, mattis ullamcorper dignissim vitae, condimentum non lorem.");
        collection.insert(doc);
        collection.insert(doc3);
        collection.insert(doc1);
        collection.insert(doc2);

        DocumentCursor cursor = collection.find(
            and(
                where("lastName").notEq("ln1"),
                where("birthDay").notEq(simpleDateFormat.parse("2012-07-01T16:02:48.440Z"))
            ),
            orderBy("lastName", SortOrder.Ascending)
                .thenOrderBy("birthDay", SortOrder.Descending)
        );

        List<Document> documents = cursor.toList();
        assertEquals(3, documents.size());

        Document document = documents.get(0);
        assertEquals("ln2", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2014-04-17T16:02:48.440Z"), document.get("birthDay"));

        document = documents.get(1);
        assertEquals("ln2", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2010-06-12T16:02:48.440Z"), document.get("birthDay"));

        document = documents.get(2);
        assertEquals("ln3", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2016-04-17T16:02:48.440Z"), document.get("birthDay"));

        FindPlan findPlan = cursor.getFindPlan();
        List<Pair<String, SortOrder>> blockingSortOrder = findPlan.getBlockingSortOrder();
        assertEquals(2, blockingSortOrder.size());

        Pair<String, SortOrder> pair = blockingSortOrder.get(0);
        assertEquals("lastName", pair.getFirst());
        assertEquals(SortOrder.Ascending, pair.getSecond());

        pair = blockingSortOrder.get(1);
        assertEquals("birthDay", pair.getFirst());
        assertEquals(SortOrder.Descending, pair.getSecond());

        assertNotNull(findPlan.getCollectionScanFilter());
        assertNull(findPlan.getIndexDescriptor());
        assertNull(findPlan.getIndexScanOrder());
    }

    @Test
    public void testSortNotCoveredByIndex() throws ParseException {
        // some field sort by index, some by memory

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName");

        Document doc = createDocument("firstName", "fn4")
            .put("lastName", "ln3")
            .put("birthDay", simpleDateFormat.parse("2016-04-17T16:02:48.440Z"))
            .put("data", new byte[]{9, 4, 8})
            .put("body", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed nunc mi, mattis ullamcorper dignissim vitae, condimentum non lorem.");
        collection.insert(doc);
        collection.insert(doc3);
        collection.insert(doc1);
        collection.insert(doc2);

        DocumentCursor cursor = collection.find(
            and(
                where("lastName").notEq("ln1"),
                where("birthDay").notEq(simpleDateFormat.parse("2012-07-01T16:02:48.440Z"))
            ),
            orderBy("lastName", SortOrder.Ascending)
                .thenOrderBy("birthDay", SortOrder.Descending)
        );

        List<Document> documents = cursor.toList();
        assertEquals(3, documents.size());

        Document document = documents.get(0);
        assertEquals("ln2", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2014-04-17T16:02:48.440Z"), document.get("birthDay"));

        document = documents.get(1);
        assertEquals("ln2", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2010-06-12T16:02:48.440Z"), document.get("birthDay"));

        document = documents.get(2);
        assertEquals("ln3", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2016-04-17T16:02:48.440Z"), document.get("birthDay"));

        FindPlan findPlan = cursor.getFindPlan();
        List<Pair<String, SortOrder>> blockingSortOrder = findPlan.getBlockingSortOrder();
        assertEquals(2, blockingSortOrder.size());

        Pair<String, SortOrder> pair = blockingSortOrder.get(0);
        assertEquals("lastName", pair.getFirst());
        assertEquals(SortOrder.Ascending, pair.getSecond());

        pair = blockingSortOrder.get(1);
        assertEquals("birthDay", pair.getFirst());
        assertEquals(SortOrder.Descending, pair.getSecond());

        assertNotNull(findPlan.getCollectionScanFilter());
        assertNotNull(findPlan.getIndexDescriptor());
        assertNull(findPlan.getIndexScanOrder());
    }

    @Test
    public void testSortByIndexPrefix() throws ParseException {
        collection.createIndex("lastName", "birthDay");

        Document doc = createDocument("firstName", "fn4")
            .put("lastName", "ln3")
            .put("birthDay", simpleDateFormat.parse("2016-04-17T16:02:48.440Z"))
            .put("data", new byte[]{9, 4, 8})
            .put("body", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed nunc mi, mattis ullamcorper dignissim vitae, condimentum non lorem.");
        collection.insert(doc);
        collection.insert(doc3);
        collection.insert(doc1);
        collection.insert(doc2);

        DocumentCursor cursor = collection.find(
            and(
                where("lastName").notEq("ln1"),
                where("birthDay").notEq(simpleDateFormat.parse("2012-07-01T16:02:48.440Z"))
            ),
            orderBy("lastName", SortOrder.Ascending)
        );

        List<Document> documents = cursor.toList();
        assertEquals(3, documents.size());

        Document document = documents.get(0);
        assertEquals("ln2", document.get("lastName"));
        // duplicate birthday will have natural sort order - ascending
        assertEquals(simpleDateFormat.parse("2010-06-12T16:02:48.440Z"), document.get("birthDay"));

        document = documents.get(1);
        assertEquals("ln2", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2014-04-17T16:02:48.440Z"), document.get("birthDay"));

        document = documents.get(2);
        assertEquals("ln3", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2016-04-17T16:02:48.440Z"), document.get("birthDay"));

        FindPlan findPlan = cursor.getFindPlan();
        assertTrue(findPlan.getBlockingSortOrder().isEmpty());
        assertNull(findPlan.getCollectionScanFilter());
        assertNotNull(findPlan.getIndexDescriptor());
        assertFalse(findPlan.getIndexScanOrder().get("lastName"));
    }

    @Test
    public void testLimitAndSkip() throws ParseException {
        // test skip and limit

        collection.createIndex("lastName", "birthDay");

        Document doc = createDocument("firstName", "fn4")
            .put("lastName", "ln3")
            .put("birthDay", simpleDateFormat.parse("2016-04-17T16:02:48.440Z"))
            .put("data", new byte[]{9, 4, 8})
            .put("body", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed nunc mi, mattis ullamcorper dignissim vitae, condimentum non lorem.");
        collection.insert(doc);
        collection.insert(doc3);
        collection.insert(doc1);
        collection.insert(doc2);

        DocumentCursor cursor = collection.find(
            and(
                where("lastName").notEq("ln1"),
                where("birthDay").notEq(simpleDateFormat.parse("2012-07-01T16:02:48.440Z"))
            ),
            orderBy("lastName", SortOrder.Ascending)
                .thenOrderBy("birthDay", SortOrder.Descending)
                .skip(2)
                .limit(1)
        );

        List<Document> documents = cursor.toList();
        assertEquals(1, documents.size());

        Document document = documents.get(0);
        assertEquals("ln3", document.get("lastName"));
        assertEquals(simpleDateFormat.parse("2016-04-17T16:02:48.440Z"), document.get("birthDay"));

        FindPlan findPlan = cursor.getFindPlan();
        assertTrue(findPlan.getBlockingSortOrder().isEmpty());
        assertNull(findPlan.getCollectionScanFilter());
        assertNotNull(findPlan.getIndexDescriptor());
        assertFalse(findPlan.getIndexScanOrder().get("lastName"));
        assertEquals(2L, (long)findPlan.getSkip());
        assertEquals(1L, (long)findPlan.getLimit());

        // reverse scan
        assertTrue(findPlan.getIndexScanOrder().get("birthDay"));
    }
}
