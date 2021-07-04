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

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class NitriteTransactionTest {
    @Test
    public void testConstructor() {
        Nitrite nitrite = mock(Nitrite.class);
        when(nitrite.getConfig()).thenReturn(new NitriteConfig());
        doReturn(new TransactionStore<>(new InMemoryStore())).when(nitrite).getStore();
        assertEquals(State.Active, (new NitriteTransaction(nitrite, new LockService())).getState());
        verify(nitrite).getConfig();
        verify(nitrite).getStore();
    }
}

