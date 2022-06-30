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

package org.dizitart.no2.transaction;

import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransactionalMapTest {
    @Test
    public void testContainsKey() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertFalse(transactionalMap.containsKey("42"));
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testContainsKey2() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));

        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        transactionalMap.put("42", "42");
        assertTrue(transactionalMap.containsKey("42"));
        assertEquals(1, transactionalMap.entries().toList().size());
    }

    @Test
    public void testContainsKey3() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));

        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        transactionalMap.put("foo", "42");
        assertFalse(transactionalMap.containsKey("42"));
        assertEquals(1, transactionalMap.entries().toList().size());
    }

    @Test
    public void testGet() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertNull(transactionalMap.get("42"));
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testGet2() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertNull(transactionalMap.get(0));
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testClear() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new InMemoryStore());
        transactionalMap.clear();
        assertTrue(transactionalMap.entries().toList().isEmpty());
        assertEquals(0L, transactionalMap.size());
        assertEquals(4, transactionalMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testValues() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertTrue(transactionalMap.values().toList().isEmpty());
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testRemove() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertNull(transactionalMap.remove("42"));
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testRemove2() {
        InMemoryMap<Object, Object> primary = new InMemoryMap<>("Map Name",
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertNull(transactionalMap.remove("42"));
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testRemove3() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));

        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        transactionalMap.put("42", "42");
        assertEquals("42", transactionalMap.remove("42"));
        assertTrue(transactionalMap.entries().toList().isEmpty());
        assertEquals(0L, transactionalMap.size());
    }

    @Test
    public void testRemove4() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));

        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        transactionalMap.put("", "42");
        assertNull(transactionalMap.remove("42"));
        assertEquals(1, transactionalMap.entries().toList().size());
    }

    @Test
    public void testKeys() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertTrue(transactionalMap.keys().toList().isEmpty());
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testPut() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new InMemoryStore());
        transactionalMap.put("42", "42");
        assertEquals(1, transactionalMap.entries().toList().size());
        assertEquals(1L, transactionalMap.size());
        assertEquals(4, transactionalMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testSize() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertEquals(0L, transactionalMap.size());
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testPutIfAbsent() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new InMemoryStore());
        assertNull(transactionalMap.putIfAbsent("Key", "Value"));
        assertEquals(1, transactionalMap.entries().toList().size());
        assertEquals(1L, transactionalMap.size());
        assertEquals(4, transactionalMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testEntries() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        assertTrue((new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))))).entries()
            .toList()
            .isEmpty());
    }

    @Test
    public void testReversedEntries() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertTrue(transactionalMap.reversedEntries().toList().isEmpty());
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testHigherKey() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertNull(transactionalMap.higherKey("42"));
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testHigherKey2() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));

        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>(null, primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        transactionalMap.putIfAbsent("Key", "Value");
        TransactionalMap<Object, Object> transactionalMap1 = new TransactionalMap<>("Map Name",
            transactionalMap, new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(null))));
        assertEquals("Key", transactionalMap1.higherKey("42"));
        assertEquals(1, transactionalMap1.entries().toList().size());
    }

    @Test
    public void testCeilingKey() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertNull(transactionalMap.ceilingKey("42"));
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testCeilingKey2() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));

        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>(null, primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        transactionalMap.put("42", "42");
        TransactionalMap<Object, Object> transactionalMap1 = new TransactionalMap<>("Map Name",
            transactionalMap, new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(null))));
        assertEquals("42", transactionalMap1.ceilingKey("42"));
        assertEquals(1, transactionalMap1.entries().toList().size());
    }

    @Test
    public void testLowerKey() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertNull(transactionalMap.lowerKey("42"));
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testFloorKey() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertNull(transactionalMap.floorKey("42"));
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testFloorKey2() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));

        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>(null, primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        transactionalMap.put("42", "42");
        TransactionalMap<Object, Object> transactionalMap1 = new TransactionalMap<>("Map Name",
            transactionalMap, new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(null))));
        assertEquals("42", transactionalMap1.floorKey("42"));
        assertEquals(1, transactionalMap1.entries().toList().size());
    }

    @Test
    public void testIsEmpty() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        assertTrue(transactionalMap.isEmpty());
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testIsEmpty2() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));

        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        transactionalMap.put("42", "42");
        assertFalse(transactionalMap.isEmpty());
        assertEquals(1, transactionalMap.entries().toList().size());
    }

    @Test
    public void testIsEmpty3() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));

        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        transactionalMap.put("42", "42");
        TransactionalMap<Object, Object> transactionalMap1 = new TransactionalMap<>("Map Name",
            transactionalMap, new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(null))));
        assertFalse(transactionalMap1.isEmpty());
        assertEquals(1, transactionalMap1.entries().toList().size());
    }

    @Test
    public void testDrop() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new InMemoryStore());
        transactionalMap.drop();
        assertTrue(transactionalMap.entries().toList().isEmpty());
        assertEquals(0L, transactionalMap.size());
        assertNull(transactionalMap.getAttributes());
    }

    @Test
    public void testClose() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new InMemoryStore());
        transactionalMap.close();
        assertTrue(transactionalMap.entries().toList().isEmpty());
        assertEquals(0L, transactionalMap.size());
        assertEquals(4, transactionalMap.getAttributes().getAttributes().size());
    }

    @Test
    public void testConstructor() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionStore<StoreConfig> transactionStore = new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
                transactionStore);
        TransactionStore<StoreConfig> transactionStore1 = new TransactionStore<>(
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        TransactionalMap<Object, Object> actualTransactionalMap = new TransactionalMap<>("Map Name",
            transactionalMap, transactionStore1);
        assertEquals("Map Name", actualTransactionalMap.getName());
        assertEquals("Map Name", transactionalMap.getName());
        assertSame(transactionStore1, actualTransactionalMap.getStore());
        assertSame(transactionStore, transactionalMap.getStore());
    }

    @Test
    public void testConstructor2() {
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionStore<>(new TransactionStore<>(null)));
        TransactionalMap<Object, Object> transactionalMap = new TransactionalMap<>("Map Name", primary2,
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        TransactionStore<StoreConfig> transactionStore = new TransactionStore<>(
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        TransactionalMap<Object, Object> actualTransactionalMap = new TransactionalMap<>("Map Name",
            transactionalMap, transactionStore);
        assertTrue(actualTransactionalMap.entries().toList().isEmpty());
        assertEquals(0L, actualTransactionalMap.size());
        assertTrue(actualTransactionalMap.isEmpty());
        assertSame(transactionStore, actualTransactionalMap.getStore());
        assertEquals("Map Name", actualTransactionalMap.getName());
        assertTrue(transactionalMap.entries().toList().isEmpty());
    }

    @Test
    public void testConstructor3() {
        TransactionStore<StoreConfig> transactionStore = new TransactionStore<>(
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))));
        TransactionalMap<Object, Object> actualTransactionalMap = new TransactionalMap<>("Map Name", null,
                transactionStore);
        assertTrue(actualTransactionalMap.entries().toList().isEmpty());
        assertEquals(0L, actualTransactionalMap.size());
        assertTrue(actualTransactionalMap.isEmpty());
        assertSame(transactionStore, actualTransactionalMap.getStore());
        assertEquals("Map Name", actualTransactionalMap.getName());
    }
}

