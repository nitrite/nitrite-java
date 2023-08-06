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

import org.dizitart.no2.store.events.StoreEventListener;
import org.h2.mvstore.MVStore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class MVStoreUtilsTest {
    @Test
    public void testOpenOrCreate() {
        MVStore actualOpenOrCreateResult = MVStoreUtils.openOrCreate(new MVStoreConfig());
        assertFalse(actualOpenOrCreateResult.isReadOnly());
        assertEquals(0L, actualOpenOrCreateResult.getVersionsToKeep());
        assertEquals(0, actualOpenOrCreateResult.getUnsavedMemory());
        assertEquals(0, actualOpenOrCreateResult.getStoreVersion());
        assertEquals(0, actualOpenOrCreateResult.getRetentionTime());
        assertEquals(2, actualOpenOrCreateResult.getMetaMap().size());
        assertEquals(Long.MAX_VALUE, actualOpenOrCreateResult.getMaxPageSize());
        assertEquals(1, actualOpenOrCreateResult.getMapNames().size());
        assertEquals(48, actualOpenOrCreateResult.getKeysPerPage());
        assertEquals(0, actualOpenOrCreateResult.getAutoCommitMemory());
    }

    @Test
    public void testOpenOrCreate2() {
        MVStoreConfig mvStoreConfig = new MVStoreConfig();
        mvStoreConfig.addStoreEventListener(mock(StoreEventListener.class));
        MVStore actualOpenOrCreateResult = MVStoreUtils.openOrCreate(mvStoreConfig);
        assertFalse(actualOpenOrCreateResult.isReadOnly());
        assertEquals(0L, actualOpenOrCreateResult.getVersionsToKeep());
        assertEquals(0, actualOpenOrCreateResult.getUnsavedMemory());
        assertEquals(0, actualOpenOrCreateResult.getStoreVersion());
        assertEquals(0, actualOpenOrCreateResult.getRetentionTime());
        assertEquals(2, actualOpenOrCreateResult.getMetaMap().size());
        assertEquals(Long.MAX_VALUE, actualOpenOrCreateResult.getMaxPageSize());
        assertEquals(1, actualOpenOrCreateResult.getMapNames().size());
        assertEquals(48, actualOpenOrCreateResult.getKeysPerPage());
        assertEquals(0, actualOpenOrCreateResult.getAutoCommitMemory());
    }
}

