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
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;

/**
 * Test case for numeric filter consistency across different numeric types.
 * 
 * @author Nitrite Team
 */
public class NumericFilterTest {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        collection = db.getCollection("numericTest");
    }

    @After
    public void tearDown() {
        if (collection != null) {
            collection.close();
        }
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void testIntLongComparisonWithoutIndex() {
        Document doc = Document.createDocument("value", 42);
        collection.insert(doc);

        // All filters should work consistently without index
        assertEquals(1, collection.find(where("value").eq(42L)).size());
        assertEquals(1, collection.find(where("value").lte(42L)).size());
        assertEquals(1, collection.find(where("value").gte(42L)).size());
        assertEquals(1, collection.find(where("value").lt(43L)).size());
        assertEquals(1, collection.find(where("value").gt(41L)).size());
    }

    @Test
    public void testIntLongComparisonWithIndex() {
        Document doc = Document.createDocument("value", 42);
        collection.insert(doc);

        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "value");

        // All filters should work consistently with index
        assertEquals(1, collection.find(where("value").eq(42L)).size());
        assertEquals(1, collection.find(where("value").lte(42L)).size());
        assertEquals(1, collection.find(where("value").gte(42L)).size());
        assertEquals(1, collection.find(where("value").lt(43L)).size());
        assertEquals(1, collection.find(where("value").gt(41L)).size());
    }

    @Test
    public void testIntDoubleComparisonWithoutIndex() {
        Document doc = Document.createDocument("value", 42);
        collection.insert(doc);

        // All filters should work consistently without index
        assertEquals(1, collection.find(where("value").eq(42.0)).size());
        assertEquals(1, collection.find(where("value").lte(42.0)).size());
        assertEquals(1, collection.find(where("value").gte(42.0)).size());
        assertEquals(1, collection.find(where("value").lt(43.0)).size());
        assertEquals(1, collection.find(where("value").gt(41.0)).size());
    }

    @Test
    public void testIntDoubleComparisonWithIndex() {
        Document doc = Document.createDocument("value", 42);
        collection.insert(doc);

        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "value");

        // All filters should work consistently with index
        assertEquals(1, collection.find(where("value").eq(42.0)).size());
        assertEquals(1, collection.find(where("value").lte(42.0)).size());
        assertEquals(1, collection.find(where("value").gte(42.0)).size());
        assertEquals(1, collection.find(where("value").lt(43.0)).size());
        assertEquals(1, collection.find(where("value").gt(41.0)).size());
    }

    @Test
    public void testLongIntComparisonWithoutIndex() {
        Document doc = Document.createDocument("value", 42L);
        collection.insert(doc);

        // All filters should work consistently without index
        assertEquals(1, collection.find(where("value").eq(42)).size());
        assertEquals(1, collection.find(where("value").lte(42)).size());
        assertEquals(1, collection.find(where("value").gte(42)).size());
        assertEquals(1, collection.find(where("value").lt(43)).size());
        assertEquals(1, collection.find(where("value").gt(41)).size());
    }

    @Test
    public void testLongIntComparisonWithIndex() {
        Document doc = Document.createDocument("value", 42L);
        collection.insert(doc);

        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "value");

        // All filters should work consistently with index
        assertEquals(1, collection.find(where("value").eq(42)).size());
        assertEquals(1, collection.find(where("value").lte(42)).size());
        assertEquals(1, collection.find(where("value").gte(42)).size());
        assertEquals(1, collection.find(where("value").lt(43)).size());
        assertEquals(1, collection.find(where("value").gt(41)).size());
    }

    @Test
    public void testMultipleNumericTypesWithoutIndex() {
        collection.insert(Document.createDocument("value", 10));
        collection.insert(Document.createDocument("value", 20L));
        collection.insert(Document.createDocument("value", 30.0));

        // Query with different numeric types
        assertEquals(1, collection.find(where("value").eq(10L)).size());
        assertEquals(1, collection.find(where("value").eq(20)).size());
        assertEquals(1, collection.find(where("value").eq(30)).size());

        assertEquals(3, collection.find(where("value").gte(10)).size());
        assertEquals(2, collection.find(where("value").gt(10L)).size());
        assertEquals(2, collection.find(where("value").lte(20.0)).size());
    }

    @Test
    public void testMultipleNumericTypesWithIndex() {
        collection.insert(Document.createDocument("value", 10));
        collection.insert(Document.createDocument("value", 20L));
        collection.insert(Document.createDocument("value", 30.0));

        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "value");

        // Query with different numeric types
        assertEquals(1, collection.find(where("value").eq(10L)).size());
        assertEquals(1, collection.find(where("value").eq(20)).size());
        assertEquals(1, collection.find(where("value").eq(30)).size());

        assertEquals(3, collection.find(where("value").gte(10)).size());
        assertEquals(2, collection.find(where("value").gt(10L)).size());
        assertEquals(2, collection.find(where("value").lte(20.0)).size());
    }
}
