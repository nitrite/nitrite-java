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

package org.dizitart.no2.collection;

import org.dizitart.no2.BaseCollectionTest;
import org.dizitart.no2.Document;
import org.junit.Test;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CollectionInsertTest extends BaseCollectionTest {

    @Test
    public void testInsert() {
        WriteResult result = collection.insert(doc1, doc2, doc3);
        assertEquals(result.getAffectedCount(), 3);

        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 3);

        for (Document document : cursor) {
            assertNotNull(document.get("firstName"));
            assertNotNull(document.get("lastName"));
            assertNotNull(document.get("birthDay"));
            assertNotNull(document.get("data"));
            assertNotNull(document.get("body"));
            assertNotNull(document.get(DOC_ID));
        }
    }

    @Test
    public void testInsertHeteroDocs() {
        Document document = createDocument("test", "Nitrite Test");

        WriteResult result = collection.insert(doc1, doc2, doc3, document);
        assertEquals(result.getAffectedCount(), 4);
    }
}
