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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionContextTest {
    @Test
    public void testConstructor() {
        assertEquals("TransactionContext(collectionName=null, journal=null, nitriteMap=null, config=null, active=true)",
            (new TransactionContext()).toString());
    }

    @Test
    public void testClose() throws Exception {
        LinkedList<JournalEntry> journalEntryList = new LinkedList<>();
        journalEntryList.add(new JournalEntry());

        TransactionContext transactionContext = new TransactionContext();
        TransactionalMap<NitriteId, Document> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<NitriteId, Document> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionalStore<>(null));
        TransactionalMap<NitriteId, Document> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionalStore<>(new TransactionalStore<>(null)));
        transactionContext
            .setNitriteMap(new TransactionalMap<>("Map Name", primary2, new InMemoryStore()));
        transactionContext.setJournal(journalEntryList);
        transactionContext.close();
        assertTrue(transactionContext.getJournal().isEmpty());
        NitriteMap<NitriteId, Document> nitriteMap = transactionContext.getNitriteMap();
        assertEquals(0L, nitriteMap.size());
        assertTrue(nitriteMap.entries().toList().isEmpty());
        assertEquals(4, nitriteMap.getAttributes().getAttributes().size());
    }
}

