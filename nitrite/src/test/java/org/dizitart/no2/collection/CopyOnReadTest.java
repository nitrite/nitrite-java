/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.collection;

import org.dizitart.no2.Nitrite;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.junit.Assert.*;

/**
 * Whatever a read hands out must not be the instance the store keeps, at any depth.
 * Otherwise a caller's in-place edit is written straight into the store, bypasses the
 * indexes, and races the background serialization of the page it lives in.
 */
public class CopyOnReadTest {
    private Nitrite db;
    private NitriteCollection collection;
    private NitriteId id;

    @Before
    public void setUp() {
        db = createDb();
        collection = db.getCollection("copy-on-read");
        Document document = Document.createDocument("name", "one")
            .put("tags", new ArrayList<>(Arrays.asList("a", "b")))
            .put("nested", Document.createDocument("list", new ArrayList<>(Arrays.asList(1, 2))));
        collection.insert(document);
        id = collection.find().firstOrNull().getId();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testCursorHandsOutIndependentCopies() {
        Document first = collection.find(where("name").eq("one")).firstOrNull();
        Document second = collection.find(where("name").eq("one")).firstOrNull();
        assertNotSame(first, second);
        assertNotSame(first.get("tags"), second.get("tags"));
    }

    @Test
    public void testMutatingNestedContainerOfFoundDocumentDoesNotReachStore() {
        Document found = collection.find(where("name").eq("one")).firstOrNull();
        ((List<Object>) found.get("tags")).add("c");
        ((List<Object>) found.get("nested", Document.class).get("list")).clear();

        Document stored = collection.find(where("name").eq("one")).firstOrNull();
        assertEquals(Arrays.asList("a", "b"), stored.get("tags"));
        assertEquals(Arrays.asList(1, 2), stored.get("nested", Document.class).get("list"));
    }

    @Test
    public void testGetByIdHandsOutIndependentCopies() {
        Document first = collection.getById(id);
        Document second = collection.getById(id);
        assertNotSame(first, second);
        assertNotSame(first.get("tags"), second.get("tags"));
    }

    @Test
    public void testMutatingGetByIdResultDoesNotReachStore() {
        Document found = collection.getById(id);
        found.put("name", "changed");
        ((List<Object>) found.get("tags")).add("c");

        assertEquals("one", collection.getById(id).get("name"));
        assertEquals(Arrays.asList("a", "b"), collection.getById(id).get("tags"));
        assertEquals(1, collection.find(where("name").eq("one")).size());
        assertEquals(0, collection.find(where("name").eq("changed")).size());
    }

    @Test
    public void testGetByIdOfMissingDocumentIsNull() {
        assertNull(collection.getById(NitriteId.createId(-1L)));
    }

    @Test
    public void testCopyThenUpdateIsTheSupportedWriteShape() {
        Document found = collection.getById(id);
        found.put("name", "two");
        collection.update(found);
        assertEquals("two", collection.getById(id).get("name"));
        assertEquals(1, collection.find(where("name").eq("two")).size());
    }
}
