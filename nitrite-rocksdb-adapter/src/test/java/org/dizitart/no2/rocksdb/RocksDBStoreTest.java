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

package org.dizitart.no2.rocksdb;

import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RocksDBStoreTest {
    @Test
    public void testConstructor() {
        assertTrue((new RocksDBStore()).isClosed());
        assertTrue((new RocksDBStore()).isClosed());
    }

    @Test
    public void testConstructor2() {
        RocksDBStore actualRocksDBStore = new RocksDBStore();
        assertFalse(actualRocksDBStore.hasUnsavedChanges());
        assertFalse(actualRocksDBStore.isReadOnly());
    }

    @Test(expected = NitriteException.class)
    public void testOpenMap() {
        RocksDBConfig rocksDBConfig = mock(RocksDBConfig.class);
        when(rocksDBConfig.objectFormatter()).thenThrow(new NitriteException("An error occurred"));

        RocksDBStore rocksDBStore = new RocksDBStore();
        rocksDBStore.setStoreConfig(rocksDBConfig);
        Class<?> keyType = Object.class;
        rocksDBStore.openMap("Map Name", keyType, Object.class);
        verify(rocksDBConfig).objectFormatter();
    }

    @Test
    public void testOpenRTree() {
        RocksDBStore rocksDBStore = new RocksDBStore();
        Class<?> keyType = Object.class;
        assertThrows(InvalidOperationException.class,
                () -> rocksDBStore.openRTree("R Tree Name", keyType, Object.class));
    }
}

