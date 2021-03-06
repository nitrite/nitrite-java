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

import org.dizitart.no2.rocksdb.formatter.KryoObjectFormatter;
import org.junit.Test;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import static org.mockito.Mockito.*;

public class ValueSetTest {
    @Test
    public void testIterator() {
        RocksDB rocksDB = mock(RocksDB.class);
        when(rocksDB.newIterator((ColumnFamilyHandle) any())).thenReturn(mock(RocksIterator.class));
        KryoObjectFormatter objectFormatter = new KryoObjectFormatter();
        (new ValueSet<>(rocksDB, null, objectFormatter, Object.class)).iterator();
        verify(rocksDB).newIterator((ColumnFamilyHandle) any());
    }
}

