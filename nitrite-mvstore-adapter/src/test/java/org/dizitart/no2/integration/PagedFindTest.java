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

package org.dizitart.no2.integration;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end tests for paged reads on an MVStore-backed database: every plan
 * shape must return exactly the same pages as slicing the full result list.
 */
public class PagedFindTest {
    private static final int SIZE = 5000;

    private final String fileName = TestUtil.getRandomTempDbFile();
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(fileName)
            .build();
        db = Nitrite.builder()
            .fieldSeparator(".")
            .loadModule(storeModule)
            .openOrCreate();

        collection = db.getCollection("paged");
        for (int i = 0; i < SIZE; i++) {
            collection.insert(Document.createDocument("value", i).put("group", i % 5));
        }
    }

    @After
    public void tearDown() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        TestUtil.deleteDb(fileName);
    }

    private static List<Integer> values(Iterable<Document> documents) {
        List<Integer> values = new ArrayList<>();
        for (Document document : documents) {
            values.add(document.get("value", Integer.class));
        }
        return values;
    }

    private static FindOptions options(String sortField, SortOrder sortOrder) {
        return sortField == null ? new FindOptions() : FindOptions.orderBy(sortField, sortOrder);
    }

    private void assertPagingMatchesFullResult(Filter filter, String sortField,
                                               SortOrder sortOrder, int pageSize) {
        List<Integer> expected = values(collection.find(filter, options(sortField, sortOrder)));

        List<Integer> paged = new ArrayList<>();
        long offset = 0;
        while (true) {
            FindOptions pageOptions = options(sortField, sortOrder)
                .skip(offset)
                .limit((long) pageSize);
            List<Integer> page = values(collection.find(filter, pageOptions));
            if (page.isEmpty()) {
                break;
            }
            assertTrue("page must not exceed page size", page.size() <= pageSize);
            paged.addAll(page);
            offset += pageSize;
        }

        assertEquals(expected, paged);
    }

    @Test
    public void testNaturalOrderPagedIterationMatchesFullIteration() {
        for (int pageSize : new int[]{1, 7, 100, SIZE, SIZE + 100}) {
            assertPagingMatchesFullResult(Filter.ALL, null, null, pageSize);
        }
    }

    @Test
    public void testSkipBeyondEndIsEmpty() {
        assertTrue(collection.find(Filter.ALL,
            new FindOptions().skip(SIZE + 1).limit(10)).toList().isEmpty());
        assertTrue(collection.find(Filter.ALL,
            new FindOptions().skip(SIZE).limit(10)).toList().isEmpty());
    }

    @Test
    public void testLastPageIsPartial() {
        List<Integer> lastPage = values(collection.find(Filter.ALL,
            new FindOptions().skip(SIZE - 3).limit(10)));
        assertEquals(3, lastPage.size());
    }

    @Test
    public void testPagingAfterRemovalsMatchesFullIteration() {
        collection.remove(where("group").eq(2));
        assertPagingMatchesFullResult(Filter.ALL, null, null, 97);
    }

    @Test
    public void testPagingWithOrderByMatchesFullIteration() {
        assertPagingMatchesFullResult(Filter.ALL, "value", SortOrder.Descending, 131);
    }

    @Test
    public void testPagingWithIndexedFilterMatchesFullIteration() {
        collection.createIndex("value");
        assertPagingMatchesFullResult(where("value").gte(SIZE / 2), null, null, 89);
    }

    @Test
    public void testPagingWithIndexedFilterAndIndexOrderMatchesFullIteration() {
        collection.createIndex("value");
        assertPagingMatchesFullResult(where("value").gte(SIZE / 2),
            "value", SortOrder.Ascending, 89);
    }

    @Test
    public void testPagingWithNonIndexedFilterMatchesFullIteration() {
        assertPagingMatchesFullResult(where("group").eq(3), null, null, 53);
    }

    @Test
    public void testPagingWithOrFilterMatchesFullIteration() {
        assertPagingMatchesFullResult(
            Filter.or(where("group").eq(1), where("group").eq(4)),
            null, null, 71);
    }

    /**
     * Paging through the whole collection must not be quadratic. The dataset is
     * larger than the page cache, so without the store-level skip every page
     * request re-reads and deserializes all skipped entries from disk (~25 full
     * scans in total, measured at ~10x the duration of one full scan); with it,
     * the paged lap costs about one full scan plus per-page seek overhead
     * (measured at ~1-2x). The 5x bound sits between the two, to stay robust
     * on slow or busy machines.
     */
    @Test
    public void testPagedIterationIsNotQuadratic() {
        int size = 20_000;
        int pageSize = 400;
        String payload = "x".repeat(1024);
        String perfFileName = TestUtil.getRandomTempDbFile();

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(perfFileName)
            .cacheSize(1) // MB, much smaller than the data, to defeat the page cache
            .build();
        try (Nitrite perfDb = Nitrite.builder()
            .fieldSeparator(".")
            .loadModule(storeModule)
            .openOrCreate()) {

            NitriteCollection perfCollection = perfDb.getCollection("paged-perf");
            for (int i = 0; i < size; i++) {
                perfCollection.insert(Document.createDocument("value", i).put("payload", payload));
            }

            long fullScanStart = System.nanoTime();
            int count = values(perfCollection.find()).size();
            long fullScanNanos = System.nanoTime() - fullScanStart;
            assertEquals(size, count);

            long pagedStart = System.nanoTime();
            int pagedCount = 0;
            for (long offset = 0; offset < size; offset += pageSize) {
                pagedCount += values(perfCollection.find(Filter.ALL,
                    new FindOptions().skip(offset).limit((long) pageSize))).size();
            }
            long pagedNanos = System.nanoTime() - pagedStart;
            assertEquals(size, pagedCount);

            double ratio = (double) pagedNanos / fullScanNanos;
            System.out.printf("paged iteration took %.1fx of a full scan%n", ratio);
            assertTrue(String.format(
                    "paged iteration took %.1fx of a full scan; skip does not seem to be pushed down",
                    ratio),
                ratio < 5);
        } finally {
            TestUtil.deleteDb(perfFileName);
        }
    }
}
