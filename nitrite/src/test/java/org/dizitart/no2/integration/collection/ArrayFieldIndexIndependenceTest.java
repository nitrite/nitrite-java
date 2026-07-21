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
 */

package org.dizitart.no2.integration.collection;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import java.util.Collections;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;

/**
 * A query's results must not depend on which indexes happen to exist.
 * {@code field.eq(x)} / {@code field.in(..)} on an array (list) field is
 * defined by the index path as element containment; before the fix the
 * collection-scan path did whole-value equality, so an indexed array-eq that
 * the planner relegated to a collection scan (e.g. when a range filter on
 * another field claimed the index) silently matched nothing.
 */
public class ArrayFieldIndexIndependenceTest {

    // days 0 & 2 tagged "todo", days 1 & 3 tagged "misc"; created_at = day*1000
    private NitriteCollection seed(boolean tagsIndex, boolean createdIndex) {
        Nitrite db = Nitrite.builder().fieldSeparator(".").openOrCreate();
        NitriteCollection c = db.getCollection("array_index_independence");
        c.remove(org.dizitart.no2.filters.Filter.ALL);
        if (tagsIndex) c.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "tags");
        if (createdIndex) c.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "created_at");
        for (long day = 0; day < 4; day++) {
            String tag = (day % 2 == 0) ? "todo" : "misc";
            c.insert(createDocument("created_at", day * 1000L).put("tags", Collections.singletonList(tag)));
        }
        return c;
    }

    @Test
    public void arrayEqMatchesByContainmentWithoutIndex() {
        NitriteCollection c = seed(false, false);
        assertEquals(2, c.find(where("tags").eq("todo")).size());
        assertEquals(0, c.find(where("tags").eq("nope")).size());
    }

    @Test
    public void arrayEqResultIsIndexIndependent() {
        assertEquals(seed(false, false).find(where("tags").eq("todo")).size(),
                seed(true, false).find(where("tags").eq("todo")).size());
    }

    @Test
    public void arrayEqCombinedWithRangeWhenBothFieldsIndexed() {
        NitriteCollection c = seed(true, true);
        // day 2 is the only "todo" inside [1000,3000]
        assertEquals(1, c.find(and(where("tags").eq("todo"), where("created_at").between(1000L, 3000L))).size());
        assertEquals(1, c.find(and(where("created_at").between(1000L, 3000L), where("tags").eq("todo"))).size());
        assertEquals(1, c.find(and(where("tags").eq("todo"),
                where("created_at").gte(1000L), where("created_at").lte(3000L))).size());
        assertEquals(1, c.find(and(where("tags").eq("todo"), where("created_at").gte(1000L))).size());
    }

    @Test
    public void arrayInMatchesByContainmentOnCollectionScan() {
        NitriteCollection c = seed(false, true);
        assertEquals(2, c.find(where("tags").in("todo", "other")).size());
        assertEquals(1, c.find(and(where("tags").in("todo"),
                where("created_at").between(1000L, 3000L))).size());
    }
}
