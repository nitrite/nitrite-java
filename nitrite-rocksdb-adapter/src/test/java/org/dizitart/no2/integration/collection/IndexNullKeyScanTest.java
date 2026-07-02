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
import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Range scans on an indexed field containing null values, on both index layouts
 * and across a database reopen. Guards the DBNull key serialization (a stored
 * null key or a DBNull scan probe must round-trip through Kryo) and the null-key
 * handling of the forward and reverse index scans.
 *
 * @author Anindya Chatterjee
 */
public class IndexNullKeyScanTest {
    private String filePath;
    private Nitrite db;

    @Before
    public void setUp() {
        filePath = getRandomTempDbFile();
        db = createDb(filePath);

        NitriteCollection collection = db.getCollection("test");
        collection.insert(Document.createDocument("idx", 0).put("value", null));
        for (int i = 1; i <= 10; i++) {
            collection.insert(Document.createDocument("idx", i).put("value", (double) i));
        }
        // non-unique -> composite key layout, unique -> classic layout
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "value");

        NitriteCollection uniqueCollection = db.getCollection("uniqueTest");
        uniqueCollection.insert(Document.createDocument("idx", 0).put("value", null));
        for (int i = 1; i <= 10; i++) {
            uniqueCollection.insert(Document.createDocument("idx", i).put("value", (double) i));
        }
        uniqueCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "value");

        // close and reopen, so the index keys are deserialized fresh from disk
        db.close();
        db = createDb(filePath);
    }

    @After
    public void tearDown() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        deleteDb(filePath);
    }

    @Test
    public void testRangeScansWithNullsNonUniqueIndex() {
        assertRangeScans(db.getCollection("test"));
    }

    @Test
    public void testRangeScansWithNullsUniqueIndex() {
        assertRangeScans(db.getCollection("uniqueTest"));
    }

    private void assertRangeScans(NitriteCollection collection) {
        assertEquals(4, collection.find(where("value").lt(5.0)).size());
        assertEquals(5, collection.find(where("value").lte(5.0)).size());
        assertEquals(5, collection.find(where("value").gt(5.0)).size());
        assertEquals(6, collection.find(where("value").gte(5.0)).size());

        // descending sort on the indexed field drives the reverse index scan
        List<Document> result = collection.find(where("value").lt(5.0),
            FindOptions.orderBy("value", SortOrder.Descending)).toList();
        assertEquals(4, result.size());
        for (Document document : result) {
            assertNotNull("null-valued document leaked into lt result", document.get("value"));
        }
    }
}
