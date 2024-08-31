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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        assertTrue(textFilter.applyOnTextIndex(new InMemoryMap<>("Map Name", null)).isEmpty());
        assertEquals("42", textFilter.getStringValue());
    }

    @Test
    public void testApplyOnIndex3() {
        TextFilter textFilter = new TextFilter("Field", "*");
        textFilter.setTextTokenizer(new EnglishTextTokenizer());
        assertThrows(FilterException.class,
            () -> textFilter.applyOnTextIndex(new InMemoryMap<>("Map Name", null)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void applyTestWhenDocStringContainsSearchString() {
        //Arrange
        TextFilter textFilter = new TextFilter("fieldString", "string");
        Document docMock = mock(Document.class);
        Pair<NitriteId, Document> pairElementMock = mock(Pair.class);
        when(pairElementMock.getSecond()).thenReturn(docMock);
        when(docMock.get(Mockito.anyString())).thenReturn("parent doc string");

        //Action
        var result = textFilter.apply(pairElementMock);
        //Assert
        assertTrue(result);

    }
}

