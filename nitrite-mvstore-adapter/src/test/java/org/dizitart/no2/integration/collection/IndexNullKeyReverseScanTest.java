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
 * Sibling of the issue 1262 regression: the reverse (descending) index scan of
 * lt/lte guarded the null index key with an identity comparison against the
 * {@code DBNull} singleton. On a reopened persistent store the stored null key
 * deserializes to a different {@code DBNull} instance, so the guard failed and
 * descending lt/lte wrongly included documents whose indexed field is null.
 *
 * @author Anindya Chatterjee
 */
public class IndexNullKeyReverseScanTest {
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
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "value");

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
    public void testDescendingLesserThanExcludesNullsAfterReopen() {
        NitriteCollection collection = db.getCollection("test");

        List<Document> ltResult = collection.find(where("value").lt(5.0),
            FindOptions.orderBy("value", SortOrder.Descending)).toList();
        assertEquals(4, ltResult.size());
        for (Document document : ltResult) {
            assertNotNull("null-valued document leaked into lt result", document.get("value"));
        }

        List<Document> lteResult = collection.find(where("value").lte(5.0),
            FindOptions.orderBy("value", SortOrder.Descending)).toList();
        assertEquals(5, lteResult.size());
        for (Document document : lteResult) {
            assertNotNull("null-valued document leaked into lte result", document.get("value"));
        }
    }

    @Test
    public void testForwardScansAfterReopen() {
        NitriteCollection collection = db.getCollection("test");

        assertEquals(4, collection.find(where("value").lt(5.0)).size());
        assertEquals(5, collection.find(where("value").lte(5.0)).size());
        assertEquals(5, collection.find(where("value").gt(5.0)).size());
        assertEquals(6, collection.find(where("value").gte(5.0)).size());
    }
}
