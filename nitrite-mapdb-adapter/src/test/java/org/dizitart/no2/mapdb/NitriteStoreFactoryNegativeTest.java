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

package org.dizitart.no2.mapdb;

import org.apache.commons.io.FileUtils;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.exceptions.NitriteException;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.mapdb.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.mapdb.TestUtil.createDb;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteStoreFactoryNegativeTest {
    private Nitrite db;
    private final String fileName = getRandomTempDbFile();

    @Test(expected = NitriteException.class)
    public void testOpenSecuredWithoutCredential() {
        db = createDb(fileName, "test-user", "test-password");
        NitriteCollection dbCollection = db.getCollection("test");
        dbCollection.insert(createDocument("test", "test"));
        db.commit();
        db.close();

        db = createDb(fileName);
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 1);
    }

    @Test(expected = NitriteException.class)
    public void testOpenUnsecuredWithCredential() {
        db = createDb(fileName);
        NitriteCollection dbCollection = db.getCollection("test");
        dbCollection.insert(createDocument("test", "test"));
        db.commit();
        db.close();

        db = createDb(fileName, "test-user", "test-password");
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 1);
    }

    @Test(expected = NitriteException.class)
    public void testWrongCredential() {
        db = createDb(fileName, "test-user", "test-password");
        NitriteCollection dbCollection = db.getCollection("test");
        dbCollection.insert(createDocument("test", "test"));
        db.commit();
        db.close();

        db = createDb(fileName, "test-user", "test-password2");
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 1);
    }

    @After
    public void cleanUp() {
        if (db != null && !db.isClosed()) {
            db.close();
        }

        FileUtils.deleteQuietly(new File(fileName));
    }
}
