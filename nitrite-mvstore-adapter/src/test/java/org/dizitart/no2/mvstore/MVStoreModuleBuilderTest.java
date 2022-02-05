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
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class MVStoreModuleBuilderTest {
    @Test
    public void testConstructor() {
        MVStoreModuleBuilder actualMvStoreModuleBuilder = new MVStoreModuleBuilder();
        MVStoreModuleBuilder actualFilePathResult = actualMvStoreModuleBuilder.filePath("Path");
        assertTrue(actualMvStoreModuleBuilder.autoCommit());
        assertFalse(actualMvStoreModuleBuilder.recoveryMode());
        assertFalse(actualMvStoreModuleBuilder.readOnly());
        assertEquals(Short.SIZE, actualMvStoreModuleBuilder.pageSplitSize());
        assertNull(actualMvStoreModuleBuilder.fileStore());
        assertEquals("Path", actualMvStoreModuleBuilder.filePath());
        assertTrue(actualMvStoreModuleBuilder.eventListeners().isEmpty());
        assertNull(actualMvStoreModuleBuilder.encryptionKey());
        assertFalse(actualMvStoreModuleBuilder.compressHigh());
        assertEquals(1024, actualMvStoreModuleBuilder.autoCommitBufferSize());
        assertEquals(Short.SIZE, actualMvStoreModuleBuilder.cacheSize());
        assertFalse(actualMvStoreModuleBuilder.compress());
        assertEquals(Short.SIZE, actualMvStoreModuleBuilder.cacheConcurrency());
        MVStoreConfig dbConfigResult = actualMvStoreModuleBuilder.dbConfig();
        assertNull(dbConfigResult.filePath());
        assertTrue(dbConfigResult.eventListeners().isEmpty());
        assertNull(dbConfigResult.encryptionKey());
        assertFalse(dbConfigResult.compressHigh());
        assertFalse(dbConfigResult.compress());
        assertEquals(0, dbConfigResult.cacheSize());
        assertEquals(0, dbConfigResult.cacheConcurrency());
        assertFalse(dbConfigResult.autoCompact());
        assertEquals(0, dbConfigResult.autoCommitBufferSize());
        assertFalse(dbConfigResult.autoCommit());
        assertEquals(0, dbConfigResult.pageSplitSize());
        assertFalse(dbConfigResult.recoveryMode());
        assertNull(dbConfigResult.fileStore());
        assertFalse(dbConfigResult.isReadOnly());
        assertSame(actualMvStoreModuleBuilder, actualFilePathResult);
    }

    @Test
    public void testConstructor2() {
        MVStoreModuleBuilder actualMvStoreModuleBuilder = new MVStoreModuleBuilder();
        assertTrue(actualMvStoreModuleBuilder.autoCommit());
        assertFalse(actualMvStoreModuleBuilder.recoveryMode());
        assertEquals(Short.SIZE, actualMvStoreModuleBuilder.pageSplitSize());
        assertTrue(actualMvStoreModuleBuilder.eventListeners().isEmpty());
        assertEquals(1024, actualMvStoreModuleBuilder.autoCommitBufferSize());
        assertEquals(Short.SIZE, actualMvStoreModuleBuilder.cacheSize());
        assertEquals(Short.SIZE, actualMvStoreModuleBuilder.cacheConcurrency());
        MVStoreConfig dbConfigResult = actualMvStoreModuleBuilder.dbConfig();
        assertTrue(dbConfigResult.eventListeners().isEmpty());
        assertFalse(dbConfigResult.isReadOnly());
    }

    @Test
    public void testFilePath() {
        String tempPath = System.getProperty("java.io.tmpdir");
        MVStoreModuleBuilder withConfigResult = MVStoreModule.withConfig();
        MVStoreModuleBuilder actualFilePathResult = withConfigResult
                .filePath(Paths.get(tempPath, "test.txt").toFile());
        assertSame(withConfigResult, actualFilePathResult);
        assertEquals(Paths.get(tempPath, "test.txt").toString(), actualFilePathResult.filePath());
    }

    @Test
    public void testAddStoreEventListener() {
        MVStoreModuleBuilder withConfigResult = MVStoreModule.withConfig();
        assertSame(withConfigResult, withConfigResult.addStoreEventListener(mock(StoreEventListener.class)));
    }
}

