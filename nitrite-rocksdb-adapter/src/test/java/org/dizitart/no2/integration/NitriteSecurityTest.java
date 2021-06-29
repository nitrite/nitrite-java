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

package org.dizitart.no2.integration;

import org.apache.commons.io.FileUtils;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.exceptions.NitriteSecurityException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.integration.TestUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteSecurityTest {
    private Nitrite db;
    private final String fileName = getRandomTempDbFile();

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testSecured() {
        db = createDb(fileName, "test-user", "test-password");
        NitriteCollection dbCollection = db.getCollection("test");
        dbCollection.insert(createDocument("test", "test"));
        db.commit();
        db.close();

        db = createDb(fileName, "test-user", "test-password");
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 1);
        db.close();
        deleteDb(fileName);
    }

    @Test
    public void testUnsecured() {
        db = createDb(fileName);
        NitriteCollection dbCollection = db.getCollection("test");
        dbCollection.insert(createDocument("test", "test"));
        db.commit();
        db.close();

        db = createDb(fileName);
        dbCollection = db.getCollection("test");
        assertEquals(dbCollection.find().size(), 1);
        db.close();
        deleteDb(fileName);
    }

    @Test
    public void testIssue116() {
        db = createDb(fileName, "test-user", "test-password");
        db.close();
        try {
            db = createDb(fileName,"test-user2", "test-password2");
        } catch (NitriteSecurityException se) {
            db = createDb(fileName,"test-user", "test-password");
            assertNotNull(db);
        } finally {
            db.close();
            deleteDb(fileName);
        }
    }

    @After
    public void cleanUp() {
        if (db != null && !db.isClosed()) {
            db.close();
        }

        FileUtils.deleteQuietly(new File(fileName));
    }
}
