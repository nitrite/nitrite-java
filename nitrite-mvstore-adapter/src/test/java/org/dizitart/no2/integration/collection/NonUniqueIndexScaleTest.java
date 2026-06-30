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

import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;

/**
 * Issue #1260: inserting thousands of documents that share the same non-unique index key must
 * scale - the old array layout re-serialized an ever-growing id list on every insert (O(n²) on
 * a persistent store). The composite-key layout makes each insert an O(log n) point write. This
 * test exercises that path at scale on a file-backed MVStore and verifies it stays correct.
 *
 * @author Anindya Chatterjee
 */
public class NonUniqueIndexScaleTest {
    private String filePath;
    private Nitrite db;

    @Before
    public void setUp() {
        filePath = getRandomTempDbFile();
        db = createDb(filePath);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        deleteDb(filePath);
    }

    @Test
    public void testManyDocumentsWithSameKey() {
        NitriteCollection collection = db.getCollection("scale");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "category");

        int perCategory = 10000;
        for (int i = 0; i < perCategory; i++) {
            collection.insert(Document.createDocument("category", "hot").put("seq", i));
            collection.insert(Document.createDocument("category", "cold").put("seq", i));
        }
        db.commit();

        // every id under a high-cardinality key must be retrievable through the index
        assertEquals(perCategory, collection.find(where("category").eq("hot")).size());
        assertEquals(perCategory, collection.find(where("category").eq("cold")).size());
        assertEquals(0, collection.find(where("category").eq("missing")).size());

        // reopen the database: the on-disk composite index must survive a round trip
        db.close();
        db = createDb(filePath);
        collection = db.getCollection("scale");
        assertEquals(perCategory, collection.find(where("category").eq("hot")).size());

        // removing one whole key must clear it without touching the other
        collection.remove(where("category").eq("hot"));
        db.commit();
        assertEquals(0, collection.find(where("category").eq("hot")).size());
        assertEquals(perCategory, collection.find(where("category").eq("cold")).size());
    }
}
