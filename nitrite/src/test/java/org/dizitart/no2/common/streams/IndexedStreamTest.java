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

package org.dizitart.no2.common.streams;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertTrue;

public class IndexedStreamTest {
    @Test
    public void testConstructor() {
        HashSet<NitriteId> nitriteIds = new HashSet<>();
        assertTrue(
            (new IndexedStream(nitriteIds, new InMemoryMap<>("Map Name", null))).toList().isEmpty());
    }

    @Test
    public void testIterator() {
        HashSet<NitriteId> nitriteIds = new HashSet<>();
        IndexedStream indexedStream = new IndexedStream(nitriteIds, new InMemoryMap<>("Map Name", null));
        indexedStream.iterator();
        assertTrue(indexedStream.toList().isEmpty());
    }
}

