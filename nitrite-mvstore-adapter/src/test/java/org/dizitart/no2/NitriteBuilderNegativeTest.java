/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2;

import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.junit.Test;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.TestUtil.createDb;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteBuilderNegativeTest {

    @Test(expected = NitriteIOException.class)
    public void testCreateReadonlyDatabase() {
        String filePath = getRandomTempDbFile();

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(filePath)
            .readOnly(true)
            .build();

        Nitrite db = Nitrite.builder()
            .loadModule(storeModule)
            .openOrCreate();
        db.close();
    }

    @Test(expected = InvalidOperationException.class)
    public void testCreateReadonlyInMemoryDatabase() {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .readOnly(true)
            .build();

        Nitrite db = Nitrite.builder()
            .loadModule(storeModule)
            .openOrCreate();
        db.close();
    }

    @Test(expected = NitriteIOException.class)
    public void testOpenWithLock() {
        String filePath = getRandomTempDbFile();

        createDb(filePath);
        createDb(filePath);
    }

    @Test(expected = NitriteIOException.class)
    public void testInvalidDirectory() {
        String filePath = "/ytgr/hfurh/frij.db";
        createDb(filePath);
    }
}
