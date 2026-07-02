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
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

/**
 * Regression test for <a href="https://github.com/nitrite/nitrite-java/issues/1258">Issue 1258</a>.
 * <p>
 * {@code in} filter on an indexed field must be planned as an index scan, not a
 * full collection scan, and must look up each value directly in the index
 * instead of scanning every index entry.
 *
 * @author Anindya Chatterjee
 */
public class Issue1258Test {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        collection = db.getCollection("issue1258");

        for (int i = 0; i < 1000; i++) {
            collection.insert(Document.createDocument("key", String.valueOf(i))
                .put("value", String.valueOf(i)));
        }
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void testInFilterUsesNonUniqueIndex() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "key");

        DocumentCursor cursor = collection.find(where("key").in("1", "5", "100", "999"));
        FindPlan plan = cursor.getFindPlan();

        assertNotNull("in filter must be planned as index scan", plan.getIndexScanFilter());
        assertNull("in filter must not fall back to collection scan", plan.getCollectionScanFilter());
        assertTrue(plan.getIndexDescriptor().getFields().getFieldNames().contains("key"));

        assertEquals(4, cursor.size());
    }

    @Test
    public void testInFilterUsesUniqueIndex() {
        collection.createIndex("key");

        DocumentCursor cursor = collection.find(where("key").in("1", "5", "100", "999"));
        FindPlan plan = cursor.getFindPlan();

        assertNotNull("in filter must be planned as index scan", plan.getIndexScanFilter());
        assertNull("in filter must not fall back to collection scan", plan.getCollectionScanFilter());

        assertEquals(4, cursor.size());
    }

    @Test
    public void testInFilterResultCorrectness() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "key");

        // values present, absent and duplicated in the lookup array
        DocumentCursor cursor = collection.find(where("key").in("42", "42", "no-such-key", "7"));
        assertEquals(2, cursor.size());
        for (Document document : cursor) {
            String key = document.get("key", String.class);
            assertTrue("42".equals(key) || "7".equals(key));
            assertEquals(key, document.get("value", String.class));
        }
    }
}
