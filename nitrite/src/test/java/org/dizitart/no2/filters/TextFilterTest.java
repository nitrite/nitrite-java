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

package org.dizitart.no2.filters;

import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class TextFilterTest {
    @Test
    public void testConstructor() {
        TextFilter actualTextFilter = new TextFilter("Field", "42");
        assertEquals("Field", actualTextFilter.getField());
        assertFalse(actualTextFilter.getObjectFilter());
        assertEquals("42", actualTextFilter.getStringValue());
    }

    @Test
    public void testToString() {
        TextFilter textFilter = new TextFilter("Field", "42");
        assertEquals("(Field like 42)", textFilter.toString());
        assertEquals("42", textFilter.getStringValue());
    }

    @Test
    public void testToString2() {
        TextFilter textFilter = new TextFilter("Field", "42");
        textFilter.setProcessed(true);
        textFilter.setObjectFilter(true);
        assertEquals("(Field like 42)", textFilter.toString());
    }

    @Test
    public void testApplyOnIndex() {
        TextFilter textFilter = new TextFilter("Field", "42");
        textFilter.setTextTokenizer(new EnglishTextTokenizer());
        assertTrue(textFilter.applyOnIndex(new InMemoryMap<>("Map Name", null)).isEmpty());
        assertEquals("42", textFilter.getStringValue());
    }

    @Test
    public void testApplyOnIndex3() {
        TextFilter textFilter = new TextFilter("Field", "*");
        textFilter.setTextTokenizer(new EnglishTextTokenizer());
        assertThrows(FilterException.class,
                () -> textFilter.applyOnIndex(new InMemoryMap<>("Map Name", null)));
    }
}

