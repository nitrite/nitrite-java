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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dizitart.no2.collection.FindOptions.orderBy;
import static org.dizitart.no2.common.SortOrder.Ascending;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;

/**
 * RocksDB orders keys by their raw serialized bytes, so the non-unique composite index layout
 * (issue #1260) relies on an order-preserving key encoding. These tests exercise the cases a
 * naive encoding would get wrong: negative numbers, variable-length strings, an on-disk round
 * trip, and a high-cardinality key.
 *
 * @author Anindya Chatterjee
 */
public class NonUniqueIndexCompositeTest {
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
    public void testNegativeNumberRangeOrdering() {
        NitriteCollection coll = db.getCollection("nums");
        for (int v = -50; v < 50; v++) {
            coll.insert(Document.createDocument("v", v).put("dup", 0));
            coll.insert(Document.createDocument("v", v).put("dup", 1));
        }
        coll.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "v");

        // a range straddling zero must be exact - this is where a byte order that ignores the
        // sign of the number breaks
        assertEquals(2 * 21, coll.find(where("v").gte(-10).and(where("v").lte(10))).size());
        assertEquals(2 * 10, coll.find(where("v").lt(-40)).size()); // v in -50..-41
        assertEquals(2 * 9, coll.find(where("v").gt(40)).size());   // v in 41..49
        assertEquals(2, coll.find(where("v").eq(-7)).size());

        // ascending order over the index must be numeric, not by raw bytes
        List<Integer> asc = new ArrayList<>();
        for (Document doc : coll.find(where("v").gte(-50), orderBy("v", Ascending))) {
            asc.add(doc.get("v", Integer.class));
        }
        assertEquals(Integer.valueOf(-50), asc.get(0));
        assertEquals(Integer.valueOf(49), asc.get(asc.size() - 1));
        for (int i = 1; i < asc.size(); i++) {
            org.junit.Assert.assertTrue("must be ascending", asc.get(i) >= asc.get(i - 1));
        }
    }

    @Test
    public void testVariableLengthStringOrdering() {
        NitriteCollection coll = db.getCollection("strs");
        // values whose lengths differ: a length-prefixed encoding would mis-order these
        List<String> values = Arrays.asList("a", "aa", "ab", "b", "ba", "bb", "c");
        for (String s : values) {
            coll.insert(Document.createDocument("s", s).put("dup", 0));
            coll.insert(Document.createDocument("s", s).put("dup", 1));
        }
        coll.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "s");

        // "aa" must sort before "b" (lexicographic), not after it (by length)
        List<String> asc = new ArrayList<>();
        for (Document doc : coll.find(where("s").gte("a"), orderBy("s", Ascending))) {
            String v = doc.get("s", String.class);
            if (asc.isEmpty() || !asc.get(asc.size() - 1).equals(v)) {
                asc.add(v);
            }
        }
        assertEquals(values, asc);

        assertEquals(2, coll.find(where("s").eq("aa")).size());
        // strings in ["aa", "b") -> aa, ab, b? exclusive upper -> aa, ab, ba? no: < "b" excludes b
        assertEquals(2 * 2, coll.find(where("s").gte("aa").and(where("s").lt("b"))).size()); // aa, ab
    }

    @Test
    public void testHighCardinalityKeyRoundTrip() {
        NitriteCollection coll = db.getCollection("scale");
        coll.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "k");
        int perKey = 5000;
        for (int i = 0; i < perKey; i++) {
            coll.insert(Document.createDocument("k", "same").put("seq", i));
            coll.insert(Document.createDocument("k", "other").put("seq", i));
        }
        db.commit();

        assertEquals(perKey, coll.find(where("k").eq("same")).size());

        // reopen: the composite index must survive a round trip on disk
        db.close();
        db = createDb(filePath);
        coll = db.getCollection("scale");
        assertEquals(perKey, coll.find(where("k").eq("same")).size());
        assertEquals(perKey, coll.find(where("k").eq("other")).size());

        coll.remove(where("k").eq("same"));
        db.commit();
        assertEquals(0, coll.find(where("k").eq("same")).size());
        assertEquals(perKey, coll.find(where("k").eq("other")).size());
    }
}
