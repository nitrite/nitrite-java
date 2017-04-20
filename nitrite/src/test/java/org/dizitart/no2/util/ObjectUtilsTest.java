/*
 * Copyright 2017 Nitrite author or authors.
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
 */

package org.dizitart.no2.util;

import org.dizitart.no2.internals.JacksonMapper;
import org.dizitart.no2.internals.NitriteMapper;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.dizitart.no2.util.ObjectUtils.*;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class ObjectUtilsTest {

    @Test
    public void testIsObjectStore() {
        assertFalse(isObjectStore(""));
        assertFalse(isObjectStore(null));
        assertFalse(isObjectStore("abcd"));
    }

    @Test
    public void testObjectName() {
        assertEquals(findObjectTypeName(String.class.getName()), String.class.getName());
        assertNull(findObjectTypeName(""));
        assertNull(findObjectTypeName(null));
    }

    @Test
    public void testObjectStoreName() {
        assertEquals(findObjectStoreName(String.class), "java.lang.String");
        assertEquals(findObjectStoreName(TestObject.class), "org.dizitart.no2.util.ObjectUtilsTest$TestObject");
    }

    @Test
    public void testIndexes() {
        NitriteMapper nitriteMapper = new JacksonMapper();
        Set<Index> indexes = extractIndices(nitriteMapper, TestObjectWithIndex.class);
        assertEquals(indexes.size(), 2);
    }

    @Index(value = "longValue")
    private class TestObject {
        private String stringValue;

        private Long longValue;
    }

    @Indices({
            @Index(value = "longValue"),
            @Index(value = "decimal")
    })
    private class TestObjectWithIndex {
        private long longValue;

        private TestObject testObject;

        private BigDecimal decimal;
    }
}
