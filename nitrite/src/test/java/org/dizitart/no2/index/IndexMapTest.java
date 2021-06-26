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

package org.dizitart.no2.index;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;

public class IndexMapTest {
    @Test
    public void testConstructor() {
        TreeMap<DBValue, Object> dbValueObjectMap = new TreeMap<>();
        IndexMap actualIndexMap = new IndexMap(dbValueObjectMap);
        List<NitriteId> terminalNitriteIds = actualIndexMap.getTerminalNitriteIds();
        assertTrue(terminalNitriteIds instanceof CopyOnWriteArrayList);
        assertFalse(actualIndexMap.isReverseScan());
        assertTrue(terminalNitriteIds.isEmpty());
        assertTrue(dbValueObjectMap.isEmpty());
    }

    @Test
    public void testConstructor2() {
        InMemoryMap<DBValue, Object> inMemoryMap = new InMemoryMap<>("Map Name", null);
        IndexMap actualIndexMap = new IndexMap(inMemoryMap);
        List<NitriteId> terminalNitriteIds = actualIndexMap.getTerminalNitriteIds();
        assertTrue(terminalNitriteIds instanceof CopyOnWriteArrayList);
        assertFalse(actualIndexMap.isReverseScan());
        assertTrue(terminalNitriteIds.isEmpty());
        assertEquals("Map Name", inMemoryMap.getName());
        assertEquals(0L, inMemoryMap.size());
        assertTrue(inMemoryMap.isEmpty());
        assertNull(inMemoryMap.getStore());
    }
}

