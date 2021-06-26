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

package org.dizitart.no2.mvstore;

import org.junit.Test;

import static org.junit.Assert.*;

public class NitriteMVStoreTest {
    @Test
    public void testConstructor() {
        NitriteMVStore actualNitriteMVStore = new NitriteMVStore();
        assertNull(actualNitriteMVStore.getStoreConfig());
        assertTrue(actualNitriteMVStore.isClosed());
        assertFalse(actualNitriteMVStore.hasUnsavedChanges());
        assertNotNull(actualNitriteMVStore.getStoreVersion());
    }

    @Test
    public void testOpenOrCreate() {
        NitriteMVStore nitriteMVStore = new NitriteMVStore();
        nitriteMVStore.setStoreConfig(new MVStoreConfig());
        nitriteMVStore.openOrCreate();
        assertFalse(nitriteMVStore.isReadOnly());
        assertFalse(nitriteMVStore.isClosed());
        assertTrue(nitriteMVStore.hasUnsavedChanges());
    }

    @Test
    public void testIsClosed() {
        assertTrue((new NitriteMVStore()).isClosed());
    }

    @Test
    public void testHasUnsavedChanges() {
        assertFalse((new NitriteMVStore()).hasUnsavedChanges());
    }

    @Test
    public void testGetStoreVersion() {
        assertNotNull((new NitriteMVStore()).getStoreVersion());
    }
}

