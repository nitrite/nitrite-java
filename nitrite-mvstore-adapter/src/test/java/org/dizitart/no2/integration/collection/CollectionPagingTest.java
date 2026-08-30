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
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Paging a collection, page by page, against the same rows read in one pass.
 *
 * <p>{@code skipBy(n)} is served by a storage-level skip that advances without decoding what it
 * passes over - by descending the tree by index on MVStore, by stepping the native iterator on
 * RocksDB. Both are a different code path from {@code next()}, so what has to be held is that they
 * land on exactly the same row: an off-by-one in the seek is a page that silently starts one row
 * late, which no existing test would notice.
 */
public class CollectionPagingTest extends BaseCollectionTest {

    private static final int ROWS = 500;
    private static final int PAGE = 25;

    private void seed() {
        List<Document> documents = new ArrayList<>(ROWS);
        for (int i = 0; i < ROWS; i++) {
            documents.add(createDocument("index", i).put("name", "row " + i));
        }
        collection.insert(documents.toArray(new Document[0]));
    }

    /** Every page, against the slice of the full scan it claims to be. */
    @Test
    public void everyPageIsTheSliceOfTheFullScanItClaimsToBe() {
        seed();

        List<Object> whole = new ArrayList<>();
        for (Document document : collection.find(ALL)) {
            whole.add(document.get("index"));
        }
        assertEquals(ROWS, whole.size());

        for (int page = 0; page * PAGE < ROWS; page++) {
            List<Object> paged = new ArrayList<>();
            for (Document document : collection.find(ALL,
                    FindOptions.skipBy((long) page * PAGE).limit(PAGE))) {
                paged.add(document.get("index"));
            }
            assertEquals("page " + page,
                    whole.subList(page * PAGE, Math.min((page + 1) * PAGE, ROWS)), paged);
        }
    }

    /** The same, once an index is answering the filter - a different stream underneath. */
    @Test
    public void anIndexedQueryPagesTheSameWay() {
        seed();
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "index");

        List<Object> whole = new ArrayList<>();
        for (Document document : collection.find(where("index").gte(100))) {
            whole.add(document.get("index"));
        }
        assertEquals(ROWS - 100, whole.size());

        for (int page = 0; page * PAGE < whole.size(); page++) {
            List<Object> paged = new ArrayList<>();
            for (Document document : collection.find(where("index").gte(100),
                    FindOptions.skipBy((long) page * PAGE).limit(PAGE))) {
                paged.add(document.get("index"));
            }
            assertEquals("page " + page,
                    whole.subList(page * PAGE, Math.min((page + 1) * PAGE, whole.size())), paged);
        }
    }

    /**
     * A filter that runs as a collection scan has to look at every document to know whether to keep
     * it, so it cannot use the cheap skip and must not try - this is the fallback path.
     */
    @Test
    public void aScannedFilterStillPagesCorrectly() {
        seed();

        List<Object> whole = new ArrayList<>();
        for (Document document : collection.find(where("name").eq("row 7").not())) {
            whole.add(document.get("index"));
        }
        assertEquals(ROWS - 1, whole.size());

        List<Object> paged = new ArrayList<>();
        for (Document document : collection.find(where("name").eq("row 7").not(),
                FindOptions.skipBy(100L).limit(PAGE))) {
            paged.add(document.get("index"));
        }
        assertEquals(whole.subList(100, 100 + PAGE), paged);
    }

    /** A skip past the end is an empty page, not an error and not the last page. */
    @Test
    public void aPagePastTheEndIsEmpty() {
        seed();

        assertTrue(collection.find(ALL, FindOptions.skipBy((long) ROWS).limit(PAGE))
                .toList().isEmpty());
        assertTrue(collection.find(ALL, FindOptions.skipBy(ROWS * 100L).limit(PAGE))
                .toList().isEmpty());
        // Exactly on the boundary: the last row is the last page's only row.
        assertEquals(1, collection.find(ALL, FindOptions.skipBy(ROWS - 1L).limit(PAGE))
                .toList().size());
    }

    /** Skipping the whole collection and skipping nothing are both edges of the same seek. */
    @Test
    public void theEdgesOfTheSkip() {
        seed();

        assertEquals(PAGE, collection.find(ALL, FindOptions.skipBy(0L).limit(PAGE))
                .toList().size());
        assertEquals(ROWS, collection.find(ALL, FindOptions.skipBy(0L).limit(ROWS * 2L))
                .toList().size());
        assertTrue(collection.find(ALL, FindOptions.skipBy((long) ROWS).limit(1))
                .toList().isEmpty());
    }

    /** An empty collection has nothing to seek into, and the seek must not fall over on it. */
    @Test
    public void anEmptyCollectionPagesToNothing() {
        collection.remove(ALL);
        assertTrue(collection.find(ALL, FindOptions.skipBy(0L).limit(PAGE)).toList().isEmpty());
        assertTrue(collection.find(ALL, FindOptions.skipBy(10L).limit(PAGE)).toList().isEmpty());
    }

    /**
     * The point of the whole exercise: a late page must not cost more than an early one.
     *
     * <p>Deliberately generous - this asserts the shape of the curve, not a number. Before the
     * storage-level skip the last page cost about 170x the first on a collection this size, because
     * every skipped row was decoded and thrown away; the assertion catches a regression to linear
     * without failing on a loaded CI box.
     */
    @Test
    public void aLatePageDoesNotCostMoreThanAnEarlyOne() {
        seed();

        long first = timePage(0);
        long last = timePage(ROWS - PAGE);
        // Warm, then measure: the first call through a page carries class loading with it.
        first = Math.min(first, timePage(0));
        last = Math.min(last, timePage(ROWS - PAGE));

        assertTrue("a page at the end of the collection cost " + last + " us against "
                        + first + " us at the start - the skip is walking the rows again",
                last < Math.max(first * 20, 20_000));
    }

    private long timePage(int skip) {
        long startedAt = System.nanoTime();
        int rows = collection.find(ALL, FindOptions.skipBy((long) skip).limit(PAGE))
                .toList().size();
        long elapsed = (System.nanoTime() - startedAt) / 1000;
        assertEquals(PAGE, rows);
        return elapsed;
    }
}
