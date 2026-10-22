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

import java.util.HashMap;
import java.util.Map;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;

/**
 * Regression test for <a href="https://github.com/nitrite/nitrite-java/issues/1335">Issue 1335</a>.
 * <p>
 * A document built from a plain {@link Map} (e.g. deserialized JSON) keeps nested
 * objects as {@link Map}s instead of {@link Document}s. Embedded field access such as
 * {@code address.city} did not descend into those maps, so filters on nested fields
 * never matched.
 *
 * @author Anindya Chatterjee
 */
public class Issue1335Test {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        collection = db.getCollection("issue1335");

        Map<String, Object> address = new HashMap<>();
        address.put("city", "bangalore");
        Map<String, Object> person = new HashMap<>();
        person.put("name", "amit");
        person.put("address", address);
        collection.insert(Document.createDocument(person));
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testNestedMapField() {
        assertEquals(1, collection.find(where("name").eq("amit")).size());
        assertEquals(1, collection.find(where("address.city").eq("bangalore")).size());
        assertEquals(0, collection.find(where("address.city").notEq("bangalore")).size());
    }
}
