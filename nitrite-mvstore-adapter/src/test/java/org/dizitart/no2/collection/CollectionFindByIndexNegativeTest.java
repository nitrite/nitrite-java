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

package org.dizitart.no2.collection;

import org.dizitart.no2.BaseCollectionTest;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionFindByIndexNegativeTest extends BaseCollectionTest {
    @Test(expected = FilterException.class)
    public void testFindTextWithWildCardMultipleWord() {
        insert();
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));

        DocumentCursor cursor = collection.find(where("body").text("*ipsum dolor*"));
        assertEquals(cursor.size(), 1);
    }

    @Test(expected = FilterException.class)
    public void testFindTextWithOnlyWildCard() {
        insert();
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));

        DocumentCursor cursor = collection.find(where("body").text("*"));
        assertEquals(cursor.size(), 1);
    }
}
