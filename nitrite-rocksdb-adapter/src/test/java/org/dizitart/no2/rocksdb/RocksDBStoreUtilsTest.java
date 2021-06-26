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
import org.dizitart.no2.store.events.StoreEventListener;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

public class RocksDBStoreUtilsTest {
    @Test
    public void testOpenOrCreate() {
        assertThrows(InvalidOperationException.class, () -> RocksDBStoreUtils.openOrCreate(new RocksDBConfig()));
    }

    @Test
    public void testOpenOrCreate2() {
        RocksDBConfig rocksDBConfig = new RocksDBConfig();
        rocksDBConfig.addStoreEventListener(mock(StoreEventListener.class));
        assertThrows(InvalidOperationException.class, () -> RocksDBStoreUtils.openOrCreate(rocksDBConfig));
    }
}

