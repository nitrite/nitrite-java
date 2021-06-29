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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RocksDBModuleBuilderTest {
    @Test
    public void testConstructor() {
        assertNull((new RocksDBModuleBuilder()).options());
    }

    @Test
    public void testConstructor2() {
        RocksDBModuleBuilder actualRocksDBModuleBuilder = new RocksDBModuleBuilder();
        RocksDBModuleBuilder actualFilePathResult = actualRocksDBModuleBuilder.filePath("Path");
        assertNull(actualRocksDBModuleBuilder.columnFamilyOptions());
        assertNull(actualRocksDBModuleBuilder.options());
        assertNull(actualRocksDBModuleBuilder.objectFormatter());
        assertEquals("Path", actualRocksDBModuleBuilder.filePath());
        assertTrue(actualRocksDBModuleBuilder.eventListeners().isEmpty());
        assertNull(actualRocksDBModuleBuilder.dbOptions());
        RocksDBConfig dbConfigResult = actualRocksDBModuleBuilder.dbConfig();
        assertNull(dbConfigResult.columnFamilyOptions());
        assertNull(dbConfigResult.options());
        assertTrue(dbConfigResult.objectFormatter() instanceof org.dizitart.no2.rocksdb.formatter.KryoObjectFormatter);
        assertFalse(dbConfigResult.isInMemory());
        assertNull(dbConfigResult.filePath());
        assertTrue(dbConfigResult.eventListeners().isEmpty());
        assertNull(dbConfigResult.dbOptions());
        assertSame(actualRocksDBModuleBuilder, actualFilePathResult);
    }

    @Test
    public void testConstructor3() {
        RocksDBModuleBuilder actualRocksDBModuleBuilder = new RocksDBModuleBuilder();
        assertTrue(actualRocksDBModuleBuilder.eventListeners().isEmpty());
        RocksDBConfig dbConfigResult = actualRocksDBModuleBuilder.dbConfig();
        assertTrue(dbConfigResult.objectFormatter() instanceof org.dizitart.no2.rocksdb.formatter.KryoObjectFormatter);
        assertTrue(dbConfigResult.eventListeners().isEmpty());
    }
}

