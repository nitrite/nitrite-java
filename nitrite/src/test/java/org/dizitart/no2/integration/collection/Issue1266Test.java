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
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Regression test for <a href="https://github.com/nitrite/nitrite-java/issues/1266">Issue 1266</a>.
 * <p>
 * When an AND filter combined an equality filter on one indexed field with two range
 * filters (a bound pair) on a second, differently-typed indexed field, the query
 * planner picked filters from both indexes into a single index scan filter set instead
 * of picking a single winning index. The mixed filter set was then applied against just
 * one of the two indexes, comparing a value of the wrong type (e.g. a {@code String}
 * against an index built on {@code Long}/{@code Double} values) and throwing a
 * {@code ClassCastException}.
 *
 * @author Anindya Chatterjee
 */
public class Issue1266Test {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        collection = db.getCollection("issue1266");

        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "item_number");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "name");

        Document doc = Document.createDocument();
        doc.put("name", "item_c");
        doc.put("item_number", 103L);
        collection.insert(doc);
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void testAndFilterAcrossDifferentlyTypedIndexes() {
        Filter eqName = FluentFilter.where("name").eq("item_c");
        Filter gteNumber = FluentFilter.where("item_number").gte(102);
        Filter lteNumber = FluentFilter.where("item_number").lte(104);

        Filter andFilter = Filter.and(eqName, gteNumber, lteNumber);

        assertEquals(1, collection.find(andFilter).size());
    }
}
