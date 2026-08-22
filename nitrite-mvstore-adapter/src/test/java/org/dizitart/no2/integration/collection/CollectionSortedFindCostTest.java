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
 *
 * @author Anindya Chatterjee
 */
public class CollectionSortedFindCostTest {
    private static final int ROWS = 2000;
    private static final int PAYLOAD = 150;

    private final String fileName = getRandomTempDbFile();
    private Nitrite db;

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
     * The same query, the same row count, the same index - only the size of the documents
     * differs. A sorted page that decodes every row pays for the payload of every row, so the
     * fat collection costs many times the lean one. A sorted page that decodes only the row it
     * returns costs about the same either way.
     * <p>
     * Both halves walk an index of identical size and shape, so that cost cancels; what does
     * not cancel is the payload of the rows each one decodes. The page is deliberately one row
     * rather than twenty: returning a fat document legitimately costs more than returning a
     * lean one, and that difference is the floor of this ratio, so the fewer rows the page
     * returns the more of the ratio is the {@link #ROWS} rows it should never have touched.
     * <p>
     * Deliberately not "sorted page vs. unsorted page", whose halves do not share the index
     * walk, nor "sorted page vs. full drain", whose halves share nothing at all. Both halves
     * here are measured back to back, under whatever load the machine is under.
     */
    @Test
    public void testSortedPageCostDoesNotFollowDocumentSize() {
        double lean = sortedPageCost("lean", 0);
        double fat = sortedPageCost("fat", PAYLOAD);

        assertTrue("a sorted page over fat documents took " + fat + "ms against " + lean
                + "ms over lean ones, same row count - it is still decoding rows it discards",
            fat < lean * 3);
    }

    private double sortedPageCost(String name, int payloadSize) {
        NitriteCollection collection = db.getCollection(name);
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "seq");

        for (int i = 0; i < ROWS; i++) {
            Document document = Document.createDocument("seq", i);
            if (payloadSize > 0) {
                List<Document> payload = new ArrayList<>(payloadSize);
                for (int w = 0; w < payloadSize; w++) {
                    payload.add(Document.createDocument("text", "word" + w).put("start", w * 300));
                }
                document.put("payload", payload);
            }
            collection.insert(document);
        }

        FindOptions page = FindOptions.orderBy("seq", SortOrder.Descending).limit(1);
        return timeOf(() -> {
            for (Document ignored : collection.find(ALL, page)) {
                // force the fetch
            }
        });
    }

    private static double timeOf(Runnable task) {
        task.run(); // warm
        long start = System.nanoTime();
        for (int i = 0; i < 3; i++) {
            task.run();
        }
        return (System.nanoTime() - start) / 3e6;
    }
}
