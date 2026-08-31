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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.assertEquals;

/**
 * Ids above 2^53 - snowflake ids, TSIDs and the like - are further apart than a double can
 * step, so an index that keyed them as doubles could not tell them apart.
 *
 * <p>Run against RocksDB as well as the in-memory store, because the fold this narrows exists
 * for byte-comparing stores in the first place: RocksDB matches on the encoded key rather than
 * through {@code compareTo}, so it is where a change to the stored form actually shows.
 */
public class CollectionLargeIdIndexTest extends BaseCollectionTest {

    // two ids 1 apart; the nearest doubles around here are 128 apart
    private static final long FIRST_ID = 870000000000000123L;
    private static final long SECOND_ID = FIRST_ID + 1;

    @Test
    public void testUniqueIndexAcceptsIdsCloserThanDoublePrecision() {
        collection.remove(org.dizitart.no2.filters.Filter.ALL);
        collection.createIndex(indexOptions(IndexType.UNIQUE), "entityId");

        collection.insert(createDocument("entityId", FIRST_ID));
        collection.insert(createDocument("entityId", SECOND_ID));

        assertEquals(2, collection.find().size());
    }

    @Test
    public void testIndexedLookupReturnsOnlyTheMatchingId() {
        collection.remove(org.dizitart.no2.filters.Filter.ALL);
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "entityId");

        collection.insert(createDocument("entityId", FIRST_ID));
        collection.insert(createDocument("entityId", SECOND_ID));

        Document found = collection.find(where("entityId").eq(FIRST_ID)).firstOrNull();
        assertEquals(1, collection.find(where("entityId").eq(FIRST_ID)).size());
        assertEquals(FIRST_ID, (long) found.get("entityId", Long.class));
    }
}
