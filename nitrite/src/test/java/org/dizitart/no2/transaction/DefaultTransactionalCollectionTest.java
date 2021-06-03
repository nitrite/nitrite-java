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

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreConfig;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultTransactionalCollectionTest {
    @Test
    public void testConstructor() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        TransactionalMap<Object, Object> primary = new TransactionalMap<>("Map Name", null, null);
        TransactionalMap<Object, Object> primary1 = new TransactionalMap<>("Map Name", primary,
            new TransactionalStore<>(null));
        TransactionalMap<Object, Object> primary2 = new TransactionalMap<>("Map Name", primary1,
            new TransactionalStore<>(new TransactionalStore<>(null)));
        when(nitriteStore.openMap(anyString(), any(), any()))
                .thenReturn(new TransactionalMap<>("Map Name", primary2, new TransactionalStore<>(
                    new TransactionalStore<>(new TransactionalStore<>(null)))));
        when(nitriteStore.hasMap(anyString())).thenReturn(true);
        TransactionalStore<StoreConfig> transactionalStore = new TransactionalStore<>(
            new TransactionalStore<>(nitriteStore));
        TransactionalConfig config = new TransactionalConfig(new NitriteConfig(), transactionalStore);

        TransactionContext transactionContext = new TransactionContext();
        transactionContext.setConfig(config);
        DefaultTransactionalCollection actualDefaultTransactionalCollection = new DefaultTransactionalCollection(null,
                transactionContext, null);
        assertNull(actualDefaultTransactionalCollection.getCollectionName());
        assertFalse(actualDefaultTransactionalCollection.isDropped());
        assertSame(transactionContext, actualDefaultTransactionalCollection.getTransactionContext());
        NitriteStore<?> store = actualDefaultTransactionalCollection.getStore();
        assertSame(transactionalStore, store);
        assertNull(actualDefaultTransactionalCollection.getPrimary());
        assertNull(actualDefaultTransactionalCollection.getNitrite());
        assertNull(actualDefaultTransactionalCollection.getNitriteMap());
        assertNull(actualDefaultTransactionalCollection.getCollectionOperations().getAttributes());
        assertNull(store.getStoreVersion());
        verify(nitriteStore, times(2)).hasMap(anyString());
        verify(nitriteStore).openMap(anyString(), any(), any());
    }
}

