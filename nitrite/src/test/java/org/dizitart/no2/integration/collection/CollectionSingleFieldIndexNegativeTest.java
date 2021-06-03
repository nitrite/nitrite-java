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
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionSingleFieldIndexNegativeTest extends BaseCollectionTest {

    @Test(expected = UniqueConstraintException.class)
    public void testCreateInvalidUniqueIndex() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.Unique), "lastName");
        assertTrue(collection.hasIndex("lastName"));
        insert();
    }

    @Test(expected = UniqueConstraintException.class)
    public void testCreateIndexOnArray() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.Unique), "data");
        assertTrue(collection.hasIndex("data"));
        // data array field has repetition, so unique constraint exception
        insert();
    }

    @Test(expected = UniqueConstraintException.class)
    public void testCreateOnInvalidField() {
        insert();
        // multiple null value will be created
        collection.createIndex(IndexOptions.indexOptions(IndexType.Unique), "my-value");
        assertTrue(collection.hasIndex("my-value"));
    }

    @Test(expected = IndexingException.class)
    public void testCreateFullTextOnNonTextField() {
        insert();
        collection.createIndex(IndexOptions.indexOptions(IndexType.Fulltext), "birthDay");
        assertTrue(collection.hasIndex("birthDay"));
    }

    @Test(expected = IndexingException.class)
    public void testDropIndexOnNonIndexedField() {
        collection.dropIndex("data");
    }

    @Test(expected = IndexingException.class)
    public void testRebuildIndexInvalid() {
        collection.rebuildIndex("unknown");
    }

    @Test(expected = IndexingException.class)
    public void createMultipleIndexTypeOnSameField() {
        collection.createIndex(indexOptions(IndexType.Unique), "lastName");
        collection.createIndex(indexOptions(IndexType.NonUnique), "lastName");
    }
}
