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
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionDeleteNegativeTest extends BaseCollectionTest {
    @Test(expected = NitriteIOException.class)
    public void testDrop() {
        collection.drop();
        insert();
        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 3);
    }

    @Test(expected = FilterException.class)
    public void testDeleteWithInvalidFilter() {
        insert();

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 3);

        WriteResult writeResult = collection.remove(where("lastName").gt(null));
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test(expected = ValidationException.class)
    public void testDeleteNullDocument() {
        insert();

        collection.remove((Document) null);
    }
}
