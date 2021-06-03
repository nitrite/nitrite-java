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

package org.dizitart.no2.store.memory;

import org.dizitart.no2.store.NitriteStore;
import org.junit.Test;

import static org.junit.Assert.*;

public class InMemoryStoreModuleTest {
    @Test
    public void testConstructor() {
        NitriteStore<?> store = (new InMemoryStoreModule()).getStore();
        assertTrue(store instanceof InMemoryStore);
        assertTrue(((InMemoryConfig) store.getStoreConfig()).eventListeners().isEmpty());
    }

    @Test
    public void testWithConfig() {
        InMemoryModuleBuilder actualWithConfigResult = InMemoryStoreModule.withConfig();
        assertTrue(actualWithConfigResult.eventListeners().isEmpty());
        assertTrue(actualWithConfigResult.dbConfig().eventListeners().isEmpty());
    }

    @Test
    public void testGetStore() {
        assertFalse((new InMemoryStoreModule()).getStore().isClosed());
    }

    @Test
    public void testPlugins() {
        assertEquals(1, (new InMemoryStoreModule()).plugins().size());
    }
}

