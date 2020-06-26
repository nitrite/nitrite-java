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
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.index.IndexType;
import org.junit.Before;
import org.junit.Test;

import static org.dizitart.no2.common.util.DocumentUtils.isSimilar;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class CollectionFieldIndexTest {
    private Nitrite db;

    @Before
    public void setUp() {
        db = NitriteBuilder.get().openOrCreate();
    }

    @Test
    public void testCollection() {
        Document doc1 = Document.createDocument("name", "Anindya")
            .put("color", new String[]{"red", "green", "blue"})
            .put("books", new Document[]{
                Document.createDocument("name", "Book ABCD")
                    .put("tag", new String[]{"tag1", "tag2"}),
                Document.createDocument("name", "Book EFGH")
                    .put("tag", new String[]{"tag3", "tag1"}),
                Document.createDocument("name", "No Tag")
            });

        Document doc2 = Document.createDocument("name", "Sandip")
            .put("color", new String[]{"purple", "yellow", "gray"})
            .put("books", new Document[]{
                Document.createDocument("name", "Book abcd")
                    .put("tag", new String[]{"tag4", "tag5"}),
                Document.createDocument("name", "Book wxyz")
                    .put("tag", new String[]{"tag3", "tag1"}),
                Document.createDocument("name", "No Tag 2")
            });

        Document doc3 = Document.createDocument("name", "Subhra")
            .put("color", new String[]{"black", "sky", "violet"})
            .put("books", new Document[]{
                Document.createDocument("name", "Book Mnop")
                    .put("tag", new String[]{"tag6", "tag2"}),
                Document.createDocument("name", "Book ghij")
                    .put("tag", new String[]{"tag3", "tag7"}),
                Document.createDocument("name", "No Tag")
            });

        NitriteCollection collection = db.getCollection("test");
        collection.createIndex("color", indexOptions(IndexType.Unique));
        collection.createIndex("books.tag", indexOptions(IndexType.NonUnique));
        collection.createIndex("books.name", indexOptions(IndexType.Fulltext));

        WriteResult writeResult = collection.insert(doc1, doc2, doc3);
        assertEquals(writeResult.getAffectedCount(), 3);

        DocumentCursor documents = collection.find(where("color").eq("red"));
        assertTrue(isSimilar(documents.firstOrNull(), doc1, "name", "color", "books"));

        documents = collection.find(where("books.name").text("abcd"));
        assertEquals(documents.size(), 2);

        documents = collection.find(where("books.tag").eq("tag2"));
        assertEquals(documents.size(), 2);

        documents = collection.find(where("books.tag").eq("tag5"));
        assertEquals(documents.size(), 1);

        documents = collection.find(where("books.tag").eq("tag10"));
        assertEquals(documents.size(), 0);
    }
}
