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
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.streams.DocumentSorter;
import org.dizitart.no2.common.tuples.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Regression test for <a href="https://github.com/nitrite/nitrite-java/issues/1261">Issue 1261</a>.
 * <p>
 * {@link DocumentSorter#compare(Pair, Pair)} returned -1 when both sort keys were
 * null, violating the {@link java.util.Comparator} contract and causing TimSort to
 * throw "Comparison method violates its general contract!" during blocking sort.
 *
 * @author Anindya Chatterjee
 */
public class Issue1261Test {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        collection = db.getCollection("issue1261");
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void testOrderByFieldWithMultipleNullsDoesNotThrow() {
        // deterministic layout from the issue report - trips TimSort's invariant
        // check when null-vs-null comparison is not 0
        Random random = new Random(65);
        for (int i = 0; i < 35; i++) {
            Document document = Document.createDocument("idx", i);
            if (!random.nextBoolean()) {
                document.put("value", random.nextDouble());
            }
            collection.insert(document);
        }

        // "value" is not indexed, so this exercises the in-memory blocking sort
        List<Document> ascending = collection
            .find(FindOptions.orderBy("value", SortOrder.Ascending)).toList();
        assertEquals(35, ascending.size());
        assertSorted(ascending, true);

        List<Document> descending = collection
            .find(FindOptions.orderBy("value", SortOrder.Descending)).toList();
        assertEquals(35, descending.size());
        assertSorted(descending, false);
    }

    @Test
    public void testCompareIsSymmetricForNullValues() {
        DocumentSorter sorter = new DocumentSorter(null,
            singletonList(new Pair<>("value", SortOrder.Ascending)));

        Pair<NitriteId, Document> nullPair1 = new Pair<>(NitriteId.newId(),
            Document.createDocument("idx", 1));
        Pair<NitriteId, Document> nullPair2 = new Pair<>(NitriteId.newId(),
            Document.createDocument("idx", 2));
        Pair<NitriteId, Document> nonNullPair = new Pair<>(NitriteId.newId(),
            Document.createDocument("idx", 3).put("value", 1.0));

        // both null keys must compare equal in both directions
        assertEquals(0, sorter.compare(nullPair1, nullPair2));
        assertEquals(0, sorter.compare(nullPair2, nullPair1));

        // null-vs-non-null must be antisymmetric
        assertEquals(-1, sorter.compare(nullPair1, nonNullPair));
        assertEquals(1, sorter.compare(nonNullPair, nullPair1));
    }

    private void assertSorted(List<Document> documents, boolean ascending) {
        // nulls always group first for ascending, last for descending
        List<Double> nonNullValues = new ArrayList<>();
        boolean nullSeen = false;
        boolean nonNullSeen = false;
        for (Document document : documents) {
            Double value = document.get("value", Double.class);
            if (value == null) {
                nullSeen = true;
                if (ascending) {
                    assertTrue("null found after non-null value in ascending sort", !nonNullSeen);
                }
            } else {
                nonNullSeen = true;
                if (!ascending) {
                    assertTrue("non-null found after null value in descending sort", !nullSeen);
                }
                nonNullValues.add(value);
            }
        }

        for (int i = 1; i < nonNullValues.size(); i++) {
            int comparison = nonNullValues.get(i - 1).compareTo(nonNullValues.get(i));
            if (ascending) {
                assertTrue("values not in ascending order", comparison <= 0);
            } else {
                assertTrue("values not in descending order", comparison >= 0);
            }
        }
    }
}
