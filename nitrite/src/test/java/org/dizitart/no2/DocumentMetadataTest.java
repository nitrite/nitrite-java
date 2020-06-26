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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.events.EventType;
import org.junit.Test;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class DocumentMetadataTest extends BaseCollectionTest {
    @Test
    public void testTimeStamp() {
        Document document = createDocument("test_key", "test_value");
        assertEquals(document.getRevision().intValue(), 0);
        assertEquals(document.getLastModifiedSinceEpoch().longValue(), 0L);

        collection.insert(document);
        document = collection.find().firstOrNull();

        assertEquals(document.getRevision().intValue(), 1);
        assertTrue(document.getLastModifiedSinceEpoch() > 0);

        long previous = document.getRevision();

        DocumentCursor cursor = collection.find(where("test_key").eq("test_value"));
        document = cursor.firstOrNull();
        document.put("another_key", "another_value");

        collection.update(document);
        cursor = collection.find(where("test_key").eq("test_value"));
        document = cursor.firstOrNull();

        assertTrue(document.getRevision() > previous);

        final long time = document.getRevision();
        final Document removed = document;

        collection.subscribe(changeInfo -> {
            if (changeInfo.getEventType() == EventType.Remove) {
                assertTrue(removed.getRevision() > time);
            }
        });

        collection.remove(document);
    }
}
