/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

package org.dizitart.no2.index;

import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IndexFieldsTest {

    @Test
    public void testIndextype() {
        IndexFields indexFields = IndexFields.create(IndexType.UNIQUE, "a", "b");
        assertEquals(indexFields.getIndexType(), IndexType.UNIQUE);
    }

    @Test
    public void testGetFields() {
        IndexFields indexFields = IndexFields.create(IndexType.UNIQUE, "a", "b");
        assertArrayEquals(indexFields.getFieldNames().toArray(), new String[] {"a", "b"});
    }

    @Test(expected = ValidationException.class)
    public void testGetFieldsWithoutFields() {
        IndexFields indexFields = IndexFields.create(IndexType.UNIQUE);
        assertArrayEquals(indexFields.getFieldNames().toArray(), new String[0]);
    }

    @Test
    public void testGetEncodedName() {
        IndexFields indexFields = IndexFields.create(IndexType.UNIQUE, "a", "b");
        assertEquals("Unique[a|b]", indexFields.getEncodedName());
    }

    @Test
    public void testToString() {
        IndexFields indexFields = IndexFields.create(IndexType.UNIQUE, "a", "b");
        assertEquals("Unique[a|b]", indexFields.toString());
    }
}
