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

import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultTransactionalCollectionTest {
    @Test
    public void testConstructor() {
        TransactionalConfig transactionalConfig = mock(TransactionalConfig.class);
        when(transactionalConfig.getNitriteStore()).thenThrow(new NotIdentifiableException("An error occurred"));

        TransactionContext transactionContext = new TransactionContext();
        transactionContext.setConfig(transactionalConfig);
        assertThrows(NotIdentifiableException.class,
            () -> new DefaultTransactionalCollection(null, transactionContext, null));
        verify(transactionalConfig).getNitriteStore();
    }

    @Test
    public void testConstructor2() {
        TransactionalConfig transactionalConfig = mock(TransactionalConfig.class);
        TransactionalStore<?> transactionalStore = new TransactionalStore<>(
            new TransactionalStore<>(new TransactionalStore<>(new InMemoryStore())));

        doReturn(transactionalStore).when(transactionalConfig).getNitriteStore();
        TransactionContext transactionContext = new TransactionContext();
        transactionContext.setConfig(transactionalConfig);
        DefaultTransactionalCollection actualDefaultTransactionalCollection = new DefaultTransactionalCollection(null,
            transactionContext, null);
        assertNull(actualDefaultTransactionalCollection.getCollectionName());
        assertFalse(actualDefaultTransactionalCollection.isDropped());
        TransactionContext transactionContext1 = actualDefaultTransactionalCollection.getTransactionContext();
        assertSame(transactionContext, transactionContext1);
        assertSame(transactionalStore, actualDefaultTransactionalCollection.getStore());
        assertNull(actualDefaultTransactionalCollection.getPrimary());
        assertNull(actualDefaultTransactionalCollection.getNitrite());
        assertNull(actualDefaultTransactionalCollection.getNitriteMap());
        assertNull(actualDefaultTransactionalCollection.getCollectionOperations().getAttributes());
        verify(transactionalConfig, times(2)).getNitriteStore();
    }
}

