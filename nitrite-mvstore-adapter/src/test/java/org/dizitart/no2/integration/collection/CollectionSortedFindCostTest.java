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
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The cost half of {@link CollectionSortedFindTest}, which needs a store that actually
 * serializes documents.
 * <p>
 * A blocking sort deserializes every stored document to read one field, so
 * {@code orderBy(indexed).limit(20)} cost what draining the whole collection cost - and the
 * gap grows with document size, not just document count. Taking the sort keys from the index
 * removes the decode: over 2000 rows carrying a 150-element array the sorted page went from
 * ~115 ms to ~2 ms.
 * <p>
 * <b>Both halves of every comparison here are the same collection shape sorted two different
 * ways</b>, never a fat collection against a lean one. That earlier framing assumed the index
 * walk cancelled between the halves, and it does not: the fat collection's index lives in a
 * file two orders of magnitude larger, so walking it costs more I/O for reasons that have
 * nothing to do with decoding. Measured cold on macOS, a fat sorted page cost 4.6x a lean one
 * <i>with the optimisation present and working</i> - through a 3x threshold, which is why this
 * guard failed on that runner and nowhere else.
 *
 * @author Anindya Chatterjee
 */
public class CollectionSortedFindCostTest {
    private static final int ROWS = 2000;
    private static final int PAYLOAD = 150;

    /**
     * Fresh collections per sample, because the cost under test is only visible on a cold one.
     * Re-querying a collection that was just written measures a cache: over these same 2000 fat
     * rows a blocking sort cost 353 ms, then 83 ms, then 0.8 ms. The old guard warmed once and
     * timed the three runs after it, so it was comparing two numbers from which the decode -
     * the whole subject of the test - had already been cached away.
     */
    private static final int SAMPLES = 3;

    private final String fileName = getRandomTempDbFile();
    private Nitrite db;
    private int collectionSeq;

    @Before
    public void setUp() {
        db = Nitrite.builder()
            .loadModule(MVStoreModule.withConfig().filePath(fileName).build())
            .openOrCreate();
    }

    @After
    public void tearDown() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        deleteDb(fileName);
    }

    /**
     * The regression detector, and it has no clock in it.
     * <p>
     * A sorted page that can be answered from an index says so in its {@link FindPlan}: the
     * planner sets {@code sortIndexDescriptor} when the query has no filter, one sort field, a
     * limit, and a simple index on exactly that field. When it is set the page reads its order
     * from the index and fetches only the rows it returns; when it is not, the blocking sort
     * decodes every stored document. Asserting on the plan tests that decision directly, and
     * cannot be made to flake by a loaded machine - which the cost guard below can.
     */
    @Test
    public void testSortedPageIsPlannedFromTheIndex() {
        NitriteCollection collection = fatCollection("planned");

        DocumentCursor fromIndex = collection.find(ALL,
            FindOptions.orderBy("seq", SortOrder.Descending).limit(1));
        assertNotNull("a limited sort on an indexed field was not planned from the index, so it "
                + "is decoding every stored document to order rows it discards",
            fromIndex.getFindPlan().getSortIndexDescriptor());

        // The control: the same query shape on a field with no index cannot use that path, so a
        // non-null descriptor above would prove nothing on its own.
        DocumentCursor blocking = collection.find(ALL,
            FindOptions.orderBy("unindexed", SortOrder.Descending).limit(1));
        assertNull("a sort on an unindexed field claims an index to read its order from",
            blocking.getFindPlan().getSortIndexDescriptor());
    }

    /**
     * The same query, over the same rows, ordered by an indexed field and by an unindexed one.
     * Only the second can be answered from an index; the first is what this optimisation
     * exists to make cheap. Everything else - row count, document size, the file, the single
     * row returned - is identical between the halves, so what is left is the decode of the
     * {@link #ROWS} rows neither query should have returned.
     * <p>
     * <b>Cold, and the best of {@link #SAMPLES}.</b> Each half is measured once on a collection
     * no query has touched, because a second look at the same collection measures a cache
     * rather than a decode. That makes any single sample noisy, so each half is sampled on
     * several fresh collections and the quickest is kept: contention can only ever add time, so
     * the quickest run is the one least contaminated by the machine.
     * <p>
     * The threshold is 2x against a measured 4.5x on the worst run of a laptop under load and
     * 45x on the best. A regression does not narrow this ratio - it removes it, because the two
     * halves become the same code path and the ratio becomes 1.
     */
    @Test
    public void testSortedPageDoesNotDecodeTheRowsItDiscards() {
        warmTheQueryPath();

        double indexed = Double.MAX_VALUE;
        double blocking = Double.MAX_VALUE;
        for (int sample = 0; sample < SAMPLES; sample++) {
            // Alternated so that any drift over the run lands on both halves alike.
            blocking = Math.min(blocking, coldSortedPageCost("unindexed"));
            indexed = Math.min(indexed, coldSortedPageCost("seq"));
        }

        assertTrue("a sorted page over an indexed field took " + indexed + "ms against "
                + blocking + "ms over an unindexed one, same rows and same documents - it is "
                + "still decoding the rows it discards", blocking > indexed * 2);
    }

    /**
     * JIT only. Run on its own collection so the measured ones stay cache-cold.
     */
    private void warmTheQueryPath() {
        NitriteCollection collection = fatCollection("warm");
        for (int i = 0; i < 3; i++) {
            drain(collection, "seq");
            drain(collection, "unindexed");
        }
    }

    private double coldSortedPageCost(String sortField) {
        NitriteCollection collection = fatCollection("cost");
        long start = System.nanoTime();
        drain(collection, sortField);
        return (System.nanoTime() - start) / 1e6;
    }

    private static void drain(NitriteCollection collection, String sortField) {
        FindOptions page = FindOptions.orderBy(sortField, SortOrder.Descending).limit(1);
        for (Document ignored : collection.find(ALL, page)) {
            // force the fetch
        }
    }

    /**
     * {@link #ROWS} rows carrying a {@link #PAYLOAD}-element array, except the one the page
     * returns. Descending on either sort field returns an end of the range, and giving that row
     * a payload would put the cost of decoding it into a measurement that is supposed to
     * contain only rows that were never returned.
     */
    private NitriteCollection fatCollection(String prefix) {
        NitriteCollection collection = db.getCollection(prefix + "-" + collectionSeq++);
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "seq");

        for (int i = 0; i < ROWS; i++) {
            Document document = Document.createDocument("seq", i).put("unindexed", ROWS - i);
            if (i != 0 && i != ROWS - 1) {
                List<Document> payload = new ArrayList<>(PAYLOAD);
                for (int w = 0; w < PAYLOAD; w++) {
                    payload.add(Document.createDocument("text", "word" + w).put("start", w * 300));
                }
                document.put("payload", payload);
            }
            collection.insert(document);
        }
        return collection;
    }
}
