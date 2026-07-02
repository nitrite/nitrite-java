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
import org.dizitart.no2.collection.NitriteId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Regression test for <a href="https://github.com/nitrite/nitrite-java/issues/1263">Issue 1263</a>.
 * <p>
 * Databases written before 4.4 store the {@code _id} field as a String. The
 * legacy-id compatibility handling covered {@code eq}/{@code getById} but not
 * {@code in}/{@code notIn}, which compared the raw stored field value and so
 * silently missed legacy String ids.
 *
 * @author Anindya Chatterjee
 */
public class Issue1263Test {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        collection = db.getCollection("issue1263");

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
    public void testEqAndGetByIdOnLegacyId() {
        // the already mitigated paths - must keep working
        assertEquals(1, collection.find(where("_id").eq(3L)).size());
        assertNotNull(collection.getById(NitriteId.createId(3L)));
    }

    @Test
    public void testInFilterOnLegacyId() {
        assertEquals(1, collection.find(where("_id").in(3L)).size());
        assertEquals(2, collection.find(where("_id").in(3L, 7L)).size());
        assertEquals(0, collection.find(where("_id").in(5L)).size());

        // String-typed lookup values must also resolve via NitriteId
        assertEquals(1, collection.find(where("_id").in("3")).size());
    }

    @Test
    public void testNotInFilterOnLegacyId() {
        // same root cause as in(): raw value comparison misses legacy String ids
        assertEquals(1, collection.find(where("_id").notIn(3L)).size());
        assertEquals(0, collection.find(where("_id").notIn(3L, 7L)).size());
        assertEquals(2, collection.find(where("_id").notIn(5L)).size());
    }

    @Test
    public void testInFilterOnNonIdFieldUnchanged() {
        assertEquals(1, collection.find(where("name").in("m3")).size());
        assertEquals(2, collection.find(where("name").in("m3", "m7")).size());
        assertEquals(1, collection.find(where("name").notIn("m3")).size());
    }
}
