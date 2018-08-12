/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.tool;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.tool.Recovery.recover;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author Anindya Chatterjee.
 */
public class RecoveryNegativeTest {
    private static final String fileName = getRandomTempDbFile();

    @Test(expected = IllegalStateException.class)
    public void testRecoverInvalid() throws IOException {
        File invalidDb = new File(fileName);
        if (invalidDb.createNewFile()) {
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            raf.setLength(raf.length() + 100);
            raf.close();

            assertFalse(recover(fileName));
        } else {
            fail("failed to create file");
        }
    }

    @After
    public void cleanUp() throws IOException {
        Files.delete(Paths.get(fileName));
    }
}
