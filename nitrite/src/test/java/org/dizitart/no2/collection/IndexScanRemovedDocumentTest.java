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
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.store.NitriteMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.junit.Assert.*;

/**
 * An index lookup and the fetch of the documents it names are not atomic: a document removed in
 * between must simply be absent from the result, never a null row that a residual filter, or the
 * caller, dereferences.
 * <p>
 * The window is reproduced by removing the document from the collection's map behind the index's
 * back, which is exactly what a concurrent remove looks like to a reader that already holds the ids.
 */
public class IndexScanRemovedDocumentTest {
    private Nitrite db;
    private NitriteCollection collection;
    private NitriteId removedId;

    @Before
    public void setUp() {
        db = createDb();
        collection = db.getCollection("race");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "group");
        for (int n = 1; n <= 3; n++) {
            collection.insert(Document.createDocument("group", "a").put("n", n));
        }
        removedId = collection.find(where("n").eq(2)).firstOrNull().getId();

        NitriteMap<NitriteId, Document> map = db.getStore().openMap("race", NitriteId.class, Document.class);
        assertNotNull(map.remove(removedId));
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testIndexScanWithResidualFilterSkipsRemovedDocument() {
        List<Document> found = collection.find(where("group").eq("a").and(where("n").gte(1))).toList();
        assertEquals(2, found.size());
        assertTrue(found.stream().noneMatch(d -> d == null));
        assertTrue(found.stream().noneMatch(d -> d.getId().equals(removedId)));
    }

    @Test
    public void testIndexScanWithoutResidualFilterSkipsRemovedDocument() {
        List<Document> found = collection.find(where("group").eq("a")).toList();
        assertEquals(2, found.size());
        assertTrue(found.stream().noneMatch(d -> d == null));
    }

    @Test
    public void testIndexScanSizeExcludesRemovedDocument() {
        assertEquals(2, collection.find(where("group").eq("a").and(where("n").gte(1))).size());
    }

    @Test
    public void testByIdOfRemovedDocumentIsEmpty() {
        assertNull(collection.find(Filter.byId(removedId)).firstOrNull());
        assertNull(collection.getById(removedId));
    }
}
