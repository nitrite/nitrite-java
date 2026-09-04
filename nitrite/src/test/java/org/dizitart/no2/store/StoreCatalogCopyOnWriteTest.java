/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.store;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import java.util.Set;

import static org.dizitart.no2.common.Constants.COLLECTION_CATALOG;
import static org.dizitart.no2.common.Constants.TAG_COLLECTIONS;
import static org.dizitart.no2.common.Constants.TAG_MAP_METADATA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * The catalog must replace the stored name set on every write, never mutate it: MVStore hands
 * out the stored instance and may be serializing it on another thread at that moment.
 */
public class StoreCatalogCopyOnWriteTest {
    @Test
    @SuppressWarnings("unchecked")
    public void testWritingAnEntryLeavesTheStoredSetUntouched() {
        InMemoryStore store = new InMemoryStore();
        StoreCatalog catalog = new StoreCatalog(store);
        catalog.writeCollectionEntry("first");

        NitriteMap<String, Document> catalogMap = store.openMap(COLLECTION_CATALOG, String.class, Document.class);
        Set<String> storedBefore = (Set<String>) catalogMap.get(TAG_COLLECTIONS).get(TAG_MAP_METADATA, Set.class);
        assertEquals(Set.of("first"), storedBefore);

        catalog.writeCollectionEntry("second");

        Set<String> storedAfter = (Set<String>) catalogMap.get(TAG_COLLECTIONS).get(TAG_MAP_METADATA, Set.class);
        assertNotSame("the write must store a new set, not the one a serializer may be reading", storedBefore, storedAfter);
        assertFalse("the previously stored set must not have been mutated", storedBefore.contains("second"));
        assertEquals(Set.of("first", "second"), storedAfter);
        assertTrue(catalog.getCollectionNames().containsAll(Set.of("first", "second")));
    }
}
