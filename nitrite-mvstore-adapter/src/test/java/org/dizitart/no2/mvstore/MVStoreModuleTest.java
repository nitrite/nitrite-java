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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MVStoreModuleTest {
    @Test
    public void testPlugins() {
        Assert.assertEquals(1, (new MVStoreModule("Path")).plugins().size());
    }

    @Test
    public void testWithConfig() {
        MVStoreModuleBuilder actualWithConfigResult = MVStoreModule.withConfig();
        assertTrue(actualWithConfigResult.autoCommit());
        assertFalse(actualWithConfigResult.recoveryMode());
        assertEquals(Short.SIZE, actualWithConfigResult.pageSplitSize());
        assertTrue(actualWithConfigResult.eventListeners().isEmpty());
        assertEquals(1024, actualWithConfigResult.autoCommitBufferSize());
        assertEquals(Short.SIZE, actualWithConfigResult.cacheSize());
        assertEquals(Short.SIZE, actualWithConfigResult.cacheConcurrency());
        MVStoreConfig dbConfigResult = actualWithConfigResult.dbConfig();
        assertTrue(dbConfigResult.eventListeners().isEmpty());
        assertFalse(dbConfigResult.isReadOnly());
    }

    @Test
    public void testGetStore() {
        assertTrue((new MVStoreModule("Path")).getStore() instanceof NitriteMVStore);
    }
}

