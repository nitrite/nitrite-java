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
 * <b>The guard here has no clock in it.</b> Two wall-clock versions of it were tried and both
 * failed on CI while the optimisation was present and working - the first comparing fat rows
 * against lean ones on macOS, the second comparing an indexed sort against an unindexed one on
 * Ubuntu, where the indexed half measured <i>slower</i> than its control. A shared runner does
 * not hold a millisecond ratio still, and no threshold rescues a measurement that inverts. The
 * decision the optimisation actually makes is recorded in the {@link FindPlan}, so that is what
 * is asserted.
 *
 * @author Anindya Chatterjee
 */
public class CollectionSortedFindCostTest {
    private static final int ROWS = 2000;
    private static final int PAYLOAD = 150;

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
