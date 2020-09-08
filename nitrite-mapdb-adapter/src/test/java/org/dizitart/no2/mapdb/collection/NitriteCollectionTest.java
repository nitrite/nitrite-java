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

package org.dizitart.no2.mapdb.collection;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.mapdb.Retry;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static org.dizitart.no2.mapdb.TestUtil.createDb;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class NitriteCollectionTest {
    private Nitrite db;

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testAttributes() {
        db = createDb();
        NitriteCollection collection = db.getCollection("test");

        Attributes attributes = new Attributes("test");
        collection.setAttributes(attributes);

        assertEquals(collection.getAttributes(), attributes);
    }

    @After
    public void cleanUp() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }
}
