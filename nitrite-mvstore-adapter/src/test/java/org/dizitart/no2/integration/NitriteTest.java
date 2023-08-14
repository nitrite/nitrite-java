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

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.mapper.EntityConverterMapper;
import org.dizitart.no2.integration.repository.data.ClassA;
import org.dizitart.no2.integration.repository.data.ClassBConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.dizitart.no2.integration.TestUtil.*;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class NitriteTest {
    private final String fileName = getRandomTempDbFile();
    private Nitrite db;

    @Before
    public void setUp() {
        db = createDb(fileName);
        EntityConverterMapper nitriteMapper = (EntityConverterMapper) db.getConfig().nitriteMapper();
        nitriteMapper.registerEntityConverter(new ClassA.ClassAConverter());
        nitriteMapper.registerEntityConverter(new ClassBConverter());


        NitriteCollection collection = db.getCollection("test");
        assertNotNull(collection);
    }

    @After
    public void cleanUp() {
        if (db != null && !db.isClosed()) {
            db.close();
        }

        deleteDb(fileName);
    }

    @Test
    public void testDestroyCollection() {
        // close the db
        db.close();

        // reopen the db
        db = createDb(fileName);

        // check if collection exists
        // the collection is noty opened yet
        db.hasCollection("test");

        // destroy the collection
        db.destroyCollection("test");

        // collection should not be present in db
        assertFalse(db.hasCollection("test"));
    }

    @Test
    public void testDestroyRepository() {
        db.getRepository(ClassA.class);

        assertTrue(db.hasRepository(ClassA.class));

        // close the db
        db.close();

        // reopen the db
        db = createDb(fileName);

        db.destroyRepository(ClassA.class);

        assertFalse(db.hasRepository(ClassA.class));
    }

    @Test
    public void testDestroyKeyedRepository() {
        db.getRepository(ClassA.class, "test");

        assertTrue(db.hasRepository(ClassA.class, "test"));

        // close the db
        db.close();

        // reopen the db
        db = createDb(fileName);

        db.destroyRepository(ClassA.class, "test");

        assertFalse(db.hasRepository(ClassA.class, "test"));
    }
}
