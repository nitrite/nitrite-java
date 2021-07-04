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

package org.dizitart.no2.integration.collection;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Anindya Chatterjee
 */
public class CollectionTest extends BaseCollectionTest {

    @Test
    public void testGetName() {
        assertEquals("test", collection.getName());
    }

    @Test
    public void testDropCollection() {
        // check if collection exists
        // the collection is noty opened yet
        db.hasCollection("test");

        // destroy the collection
        collection.drop();

        // collection should not be present in db
        assertFalse(db.hasCollection("test"));

        assertFalse(collection.isOpen());
    }

    @Test
    public void testCloseConnection() {
        collection.close();

        assertFalse(collection.isOpen());
    }

    @Test(expected = NitriteIOException.class)
    public void testDropAfterClose() {
        collection.close();

        assertFalse(collection.isOpen());

        collection.drop();
    }

    @Test(expected = NitriteIOException.class)
    public void testOperationAfterDrop() {
        collection.drop();

        collection.insert(Document.createDocument("test", "test"));
    }
}
