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

package org.dizitart.no2.util;

import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.mapper.JacksonMapper;
import org.dizitart.no2.mapper.NitriteMapper;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

import static org.dizitart.no2.util.ObjectUtils.extractIndices;
import static org.dizitart.no2.util.ObjectUtils.findObjectStoreName;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class ObjectUtilsNegativeTest {
    @Test(expected = ValidationException.class)
    public void testObjectStoreNameInvalid() {
        assertEquals(findObjectStoreName(null), null);
    }

    @Test(expected = IndexingException.class)
    public void testInvalidIndexNonComparable() {
        NitriteMapper nitriteMapper = new JacksonMapper();
        Set<Index> indexes = extractIndices(nitriteMapper, ObjectWithNonComparableIndex.class);
        assertEquals(indexes.size(), 2);
    }

    @Test(expected = IndexingException.class)
    public void testInvalidIndexComparableAndIterable() {
        NitriteMapper nitriteMapper = new JacksonMapper();
        extractIndices(nitriteMapper, ObjectWithIterableIndex.class);
    }

    @Index(value = "testClass")
    private static class ObjectWithNonComparableIndex {
        private ObjectUtilsTest testClass;
    }

    @Index(value = "testClass")
    private static class ObjectWithIterableIndex {
        private TestClass testClass;
    }

    private static class TestClass implements Comparable<TestClass>, Iterable<Long> {
        @Override
        public int compareTo(TestClass o) {
            return 0;
        }

        @Override
        public Iterator<Long> iterator() {
            return null;
        }
    }
}
