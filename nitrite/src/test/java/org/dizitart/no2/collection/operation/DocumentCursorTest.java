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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.junit.Test;

import java.util.Iterator;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class DocumentCursorTest {

    @Test
    public void testFindResult() {
        Nitrite db = NitriteBuilder.get().openOrCreate();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        DocumentCursor result = collection.find();
        assertTrue(result instanceof DocumentCursorImpl);
    }

    @Test(expected = InvalidOperationException.class)
    public void testIteratorRemove() {
        Nitrite db = NitriteBuilder.get().openOrCreate();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        DocumentCursor cursor = collection.find();
        Iterator<Document> iterator = cursor.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    @Test
    public void testValidateProjection() {
        Nitrite db = NitriteBuilder.get().openOrCreate();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        Document projection = createDocument("first", createDocument("second", null));
        ReadableStream<Document> project = collection.find().project(projection);
        assertNotNull(project);
    }
}
