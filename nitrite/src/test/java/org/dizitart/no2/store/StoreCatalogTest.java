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

package org.dizitart.no2.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

public class StoreCatalogTest {
    @Test
    public void testConstructor() {
        assertTrue((new StoreCatalog(new InMemoryStore())).getCollectionNames().isEmpty());
    }

    @Test
    public void testWriteCollectionEntry() {
        StoreCatalog storeCatalog = new StoreCatalog(new InMemoryStore());
        storeCatalog.writeCollectionEntry("Name");
        assertEquals(1, storeCatalog.getCollectionNames().size());
        assertTrue(storeCatalog.getRepositoryNames().isEmpty());
    }

    @Test
    public void testWriteRepositoryEntry() {
        StoreCatalog storeCatalog = new StoreCatalog(new InMemoryStore());
        storeCatalog.writeRepositoryEntry("Name");
        assertTrue(storeCatalog.getCollectionNames().isEmpty());
        assertEquals(1, storeCatalog.getRepositoryNames().size());
    }

    @Test
    public void testWriteKeyedRepositoryEntries() {
        StoreCatalog storeCatalog = new StoreCatalog(new InMemoryStore());
        storeCatalog.writeKeyedRepositoryEntry("Name");
        assertTrue(storeCatalog.getCollectionNames().isEmpty());
    }

    @Test
    public void testGetCollectionNames() {
        assertTrue((new StoreCatalog(new InMemoryStore())).getCollectionNames().isEmpty());
    }

    @Test
    public void testGetRepositoryNames() {
        assertTrue((new StoreCatalog(new InMemoryStore())).getRepositoryNames().isEmpty());
    }

    @Test
    public void testGetKeyedRepositoryNames() {
        assertTrue((new StoreCatalog(new InMemoryStore())).getKeyedRepositoryNames().isEmpty());
    }

    @Test
    public void testRemove() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        (new StoreCatalog(new InMemoryStore())).remove("Name");
    }
}

