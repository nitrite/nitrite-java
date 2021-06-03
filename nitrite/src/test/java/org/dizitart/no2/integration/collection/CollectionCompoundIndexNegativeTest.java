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

import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class CollectionCompoundIndexNegativeTest extends BaseCollectionTest {

    @Test(expected = UniqueConstraintException.class)
    public void testCreateInvalidUniqueIndex() {
        doc1 = createDocument("firstName", "fn3")
            .put("lastName", "ln2")
            .put("birthDay", new Date())
            .put("data", new byte[]{1, 2, 3})
            .put("list", Arrays.asList("one", "two", "three"))
            .put("body", "a quick brown fox jump over the lazy dog");

        collection.createIndex("lastName", "firstName");
        insert();
    }

    @Test(expected = UniqueConstraintException.class)
    public void testCreateUniqueMultiKeyIndexOnArray() {
        collection.createIndex("data", "lastName");
        insert();
    }

    @Test(expected = UniqueConstraintException.class)
    public void testCreateOnInvalidField() {
        insert();
        // multiple null value will be created
        collection.createIndex( "my-value", "lastName");
    }

    @Test(expected = IndexingException.class)
    public void testDropIndexOnNonIndexedField() {
        collection.dropIndex("data", "firstName");
    }

    @Test(expected = IndexingException.class)
    public void testRebuildIndexInvalid() {
        collection.rebuildIndex("unknown", "firstName");
    }

    @Test(expected = IndexingException.class)
    public void createMultipleIndexTypeOnSameFields() {
        collection.createIndex(indexOptions(IndexType.Unique), "lastName", "firstName");
        collection.createIndex(indexOptions(IndexType.NonUnique), "lastName", "firstName");
    }

    @Test(expected = IndexingException.class)
    public void testIndexAlreadyExists() {
        collection.createIndex(indexOptions(IndexType.Unique), "firstName", "lastName");
        assertTrue(collection.hasIndex("firstName"));
        collection.createIndex(indexOptions(IndexType.NonUnique), "firstName", "lastName");
    }

    @Test(expected = IndexingException.class)
    public void testCreateCompoundTextIndex() {
        collection.createIndex(indexOptions(IndexType.Fulltext), "body", "lastName");
    }

    @Test(expected = IndexingException.class)
    public void testCreateMultiKeyIndexSecondField() {
        collection.createIndex(indexOptions(IndexType.NonUnique), "lastName", "data");
        assertTrue(collection.hasIndex("lastName"));

        insert();
    }
}
