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

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.dizitart.no2.filters.Filter.ALL;
import static org.junit.Assert.assertEquals;

/**
 * Regression tests for {@code find(orderBy(field).limit(n))} on an indexed field.
 * <p>
 * The blocking sort collects and fully deserializes <em>every</em> document matching the
 * filter before the first row can be returned, so a 20-row page cost the same as draining
 * the whole collection - and an index on the sort field bought nothing, because the index
 * was only ever used to filter. Sorted, limited reads now take their sort keys from the
 * index and fetch only the documents they return.
 * <p>
 * The change must be invisible: these tests pin the result of every sorted query against
 * the same query on an unindexed collection, including the case where the index is not a
 * faithful stand-in for the collection (a multi-valued field is indexed once per element)
 * and the blocking sort has to run anyway.
 *
 * @author Anindya Chatterjee
 */
public class CollectionSortedFindTest {
    private Nitrite db;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    /**
     * Documents with a distinct {@code seq}, a {@code bucket} that repeats (so sorts have
     * ties), and a {@code name} for string ordering.
     */
    private static void seed(NitriteCollection collection, int count) {
        for (int i = 0; i < count; i++) {
            collection.insert(Document.createDocument("seq", i)
                .put("bucket", i % 5)
                .put("name", String.format("name-%04d", count - i)));
        }
    }

    private static List<Object> read(NitriteCollection collection, FindOptions options, String field) {
        List<Object> values = new ArrayList<>();
        for (Document document : collection.find(ALL, options)) {
            values.add(document.get(field));
        }
        return values;
    }

    /**
     * Runs {@code options} against an indexed and an unindexed copy of the same data and
     * asserts the two agree - document for document, in order.
     */
    private void assertIndexMatchesScan(Consumer<NitriteCollection> seeder, FindOptions options, String field) {
        NitriteCollection indexed = db.getCollection("indexed");
        indexed.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "seq");
        indexed.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "bucket");
        indexed.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "name");
        seeder.accept(indexed);

        NitriteCollection scanned = db.getCollection("scanned");
        seeder.accept(scanned);

        assertEquals("index-ordered sort disagreed with the blocking sort",
            read(scanned, options, field), read(indexed, options, field));
    }

    @Test
    public void testSortedPageMatchesFullScanAscending() {
        assertIndexMatchesScan(c -> seed(c, 500),
            FindOptions.orderBy("seq", SortOrder.Ascending).limit(20), "seq");
    }

    @Test
    public void testSortedPageMatchesFullScanDescending() {
        assertIndexMatchesScan(c -> seed(c, 500),
            FindOptions.orderBy("seq", SortOrder.Descending).limit(20), "seq");
    }

    @Test
    public void testDeepPageMatchesFullScan() {
        assertIndexMatchesScan(c -> seed(c, 500),
            FindOptions.orderBy("seq", SortOrder.Descending).skip(400).limit(20), "seq");
    }

    @Test
    public void testTiesBreakTheSameWay() {
        // 100 documents share each bucket value; the order within a tie group must not change
        assertIndexMatchesScan(c -> seed(c, 500),
            FindOptions.orderBy("bucket", SortOrder.Ascending).limit(50), "seq");
    }

    @Test
    public void testTiesBreakTheSameWayDescending() {
        assertIndexMatchesScan(c -> seed(c, 500),
            FindOptions.orderBy("bucket", SortOrder.Descending).limit(50), "seq");
    }

    @Test
    public void testStringSortMatchesFullScan() {
        assertIndexMatchesScan(c -> seed(c, 200),
            FindOptions.orderBy("name", SortOrder.Ascending).limit(20), "name");
    }

    @Test
    public void testMissingSortFieldSortsFirst() {
        // a document with no seq is indexed under null, and null sorts before everything
        assertIndexMatchesScan(c -> {
            seed(c, 100);
            c.insert(Document.createDocument("bucket", 0).put("name", "no-seq-a"));
            c.insert(Document.createDocument("bucket", 1).put("name", "no-seq-b"));
        }, FindOptions.orderBy("seq", SortOrder.Ascending).limit(10), "name");
    }

    @Test(expected = InvalidOperationException.class)
    public void testMultiValuedFieldFallsBackToBlockingSort() {
        // an array value is indexed once per element, so the index holds more entries than
        // the collection holds documents; ordering from it would return a document twice.
        // The blocking sort refuses to compare a list against a number, and the indexed
        // collection must refuse it in exactly the same way rather than quietly answering
        // from the index.
        NitriteCollection collection = db.getCollection("multi-valued");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "seq");
        seed(collection, 50);
        collection.insert(Document.createDocument("seq", Arrays.asList(3, 9)).put("name", "multi"));

        read(collection, FindOptions.orderBy("seq", SortOrder.Ascending).limit(20), "name");
    }

    @Test
    public void testPagingCoversTheCollectionExactlyOnce() {
        NitriteCollection collection = db.getCollection("paged");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "bucket");
        seed(collection, 200);

        List<Object> paged = new ArrayList<>();
        for (int page = 0; page < 10; page++) {
            paged.addAll(read(collection,
                FindOptions.orderBy("bucket", SortOrder.Descending).skip(page * 20L).limit(20), "seq"));
        }

        List<Object> allAtOnce = read(collection,
            FindOptions.orderBy("bucket", SortOrder.Descending).limit(200), "seq");

        assertEquals("paging lost or duplicated rows", 200, paged.size());
        assertEquals("paged order differs from a single read", allAtOnce, paged);
    }

    @Test
    public void testUniqueIndexSortMatchesFullScan() {
        NitriteCollection indexed = db.getCollection("unique-indexed");
        indexed.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "seq");
        seed(indexed, 200);

        NitriteCollection scanned = db.getCollection("unique-scanned");
        seed(scanned, 200);

        FindOptions options = FindOptions.orderBy("seq", SortOrder.Descending).limit(20);
        assertEquals(read(scanned, options, "seq"), read(indexed, options, "seq"));
    }

    @Test
    public void testUnlimitedSortIsUnchanged() {
        // with no limit every document is fetched anyway, so the index path is not worth
        // taking; the result must still be the plain sorted collection
        NitriteCollection collection = db.getCollection("unlimited");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "seq");
        seed(collection, 100);

        List<Object> sorted = read(collection, FindOptions.orderBy("seq", SortOrder.Descending), "seq");
        assertEquals(100, sorted.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(99 - i, sorted.get(i));
        }
    }

}
