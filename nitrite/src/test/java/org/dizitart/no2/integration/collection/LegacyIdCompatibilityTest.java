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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;

/**
 * Siblings of the issue 1263 regression: every id-based query path must resolve
 * the search term through {@link org.dizitart.no2.collection.NitriteId}, so that
 * legacy String {@code _id} values written by pre-4.4 databases keep matching
 * regardless of the query value type or the filter used.
 *
 * @author Anindya Chatterjee
 */
public class LegacyIdCompatibilityTest {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        collection = db.getCollection("legacyId");

        // legacy-style _id: a numeric String, as written by pre-4.4 databases
        collection.insert(Document.createDocument("_id", "3").put("name", "m3"));
        collection.insert(Document.createDocument("_id", "7").put("name", "m7"));
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void testEqWithNonLongSearchTerm() {
        // the byId fast path used a blind (long) cast, so a String or Integer
        // search term threw ClassCastException instead of matching
        assertEquals(1, collection.find(where("_id").eq("3")).size());
        assertEquals(1, collection.find(where("_id").eq(3)).size());
        assertEquals(0, collection.find(where("_id").eq("5")).size());
    }

    @Test
    public void testNotEqOnLegacyId() {
        // notEq compared the raw stored String value, so it failed to exclude
        assertEquals(1, collection.find(where("_id").notEq(3L)).size());
        assertEquals(1, collection.find(where("_id").notEq("3")).size());
        assertEquals(2, collection.find(where("_id").notEq(5L)).size());
    }

    @Test
    public void testNegatedEqOnLegacyId() {
        // not(eq) runs EqualsFilter.apply during collection scan, which also
        // compared the raw stored String value
        assertEquals(1, collection.find(where("_id").eq(3L).not()).size());
        assertEquals(1, collection.find(where("_id").eq("3").not()).size());
        assertEquals(2, collection.find(where("_id").eq(5L).not()).size());
    }

    @Test
    public void testNonIdFieldsUnaffected() {
        assertEquals(1, collection.find(where("name").eq("m3")).size());
        assertEquals(1, collection.find(where("name").notEq("m3")).size());
        assertEquals(1, collection.find(where("name").eq("m3").not()).size());
    }
}
