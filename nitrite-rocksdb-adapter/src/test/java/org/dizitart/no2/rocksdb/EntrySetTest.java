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

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class EntrySetTest {
    @Test
    public void testIterator() {
        RocksDB rocksDB = mock(RocksDB.class);
        when(rocksDB.newIterator((ColumnFamilyHandle) any())).thenReturn(mock(RocksIterator.class));
        KryoObjectFormatter objectFormatter = new KryoObjectFormatter();
        Class<?> keyType = Object.class;
        (new EntrySet<>(rocksDB, null, objectFormatter, keyType, Object.class, true)).iterator();
        verify(rocksDB).newIterator((ColumnFamilyHandle) any());
    }

    @Test
    public void testIterator2() {
        RocksDB rocksDB = mock(RocksDB.class);
        when(rocksDB.newIterator((ColumnFamilyHandle) any())).thenReturn(mock(RocksIterator.class));
        KryoObjectFormatter objectFormatter = new KryoObjectFormatter();
        Class<?> keyType = Object.class;
        (new EntrySet<>(rocksDB, null, objectFormatter, keyType, Object.class, false)).iterator();
        verify(rocksDB).newIterator((ColumnFamilyHandle) any());
    }

    @Test
    public void hasNextStaysFalseWithoutTouchingTheClosedIterator() {
        // hasNext() is idempotent by contract and this one closes the native iterator the first
        // time it answers false, so a second call must not reach it again. On a real RocksIterator
        // that second call is isValid() on a freed handle: an AssertionError under -ea, and a
        // SIGSEGV without it. BoundedStream's skip loop provokes it on any find(skipBy(n)) past
        // the end of a collection.
        RocksIterator rawIterator = mock(RocksIterator.class);
        RocksDB rocksDB = mock(RocksDB.class);
        when(rocksDB.newIterator((ColumnFamilyHandle) any())).thenReturn(rawIterator);
        when(rawIterator.isValid()).thenReturn(false);
        when(rawIterator.isOwningHandle()).thenReturn(true, false);
        KryoObjectFormatter objectFormatter = new KryoObjectFormatter();

        Iterator<?> iterator = (new EntrySet<>(rocksDB, null, objectFormatter, Object.class, Object.class, false)).iterator();
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());

        verify(rawIterator, times(1)).isValid();
        verify(rawIterator, times(1)).close();
    }
}
