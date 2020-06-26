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

import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class CustomFilterTest extends BaseCollectionTest {

    @Test
    public void testCustomFilter() {
        insert();
        collection.createIndex("firstName", indexOptions(IndexType.NonUnique));
        DocumentCursor cursor = collection.find(element -> element.getValue().get("firstName", String.class)
            .equalsIgnoreCase("FN1"));

        assertEquals(cursor.size(), 1);
        assertEquals(cursor.firstOrNull().get("firstName"), "fn1");
    }
}
