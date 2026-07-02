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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;

/**
 * Regression test for <a href="https://github.com/nitrite/nitrite-java/issues/1262">Issue 1262</a>.
 * <p>
 * On an indexed field containing null values, {@code lt}/{@code lte} returned an
 * empty result because the forward index scan started at the leading null key,
 * whose {@code DBNull} sentinel immediately terminated the scan loop.
 *
 * @author Anindya Chatterjee
 */
public class Issue1262Test {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        collection = db.getCollection("issue1262");

        collection.insert(Document.createDocument("idx", 0).put("value", null));
        for (int i = 1; i <= 10; i++) {
            collection.insert(Document.createDocument("idx", i).put("value", (double) i));
        }
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void testIndexedLesserThanWithNullValues() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "value");

        assertEquals(4, collection.find(where("value").lt(5.0)).size());
        assertEquals(5, collection.find(where("value").lte(5.0)).size());
        assertEquals(5, collection.find(where("value").gt(5.0)).size());
        assertEquals(6, collection.find(where("value").gte(5.0)).size());
    }

    @Test
    public void testIndexedLesserThanWithNullValuesUniqueIndex() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "value");

        assertEquals(4, collection.find(where("value").lt(5.0)).size());
        assertEquals(5, collection.find(where("value").lte(5.0)).size());
    }

    @Test
    public void testNonIndexedLesserThanWithNullValues() {
        // sanity: collection scan path was never affected
        assertEquals(4, collection.find(where("value").lt(5.0)).size());
        assertEquals(5, collection.find(where("value").lte(5.0)).size());
    }

    @Test
    public void testIndexedLesserThanWithNullValuesReverseScan() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "value");

        // descending sort on the indexed field drives the reverse index scan
        List<Document> result = collection.find(where("value").lt(5.0),
            FindOptions.orderBy("value", SortOrder.Descending)).toList();
        assertEquals(4, result.size());
        for (int i = 0; i < result.size(); i++) {
            assertEquals(4.0 - i, result.get(i).get("value", Double.class), 0.0);
        }

        List<Document> lteResult = collection.find(where("value").lte(5.0),
            FindOptions.orderBy("value", SortOrder.Descending)).toList();
        assertEquals(5, lteResult.size());
    }
}
