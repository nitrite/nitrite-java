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

package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteBuilderTest;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.common.streams.DocumentStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RepositoryOperationsTest {
    @Test
    public void testConstructor() {
        Class<?> type = Object.class;
        assertThrows(ValidationException.class,
            () -> new RepositoryOperations(type, new NitriteBuilderTest.CustomNitriteMapper(), null));
    }

    @Test(expected = ObjectMappingException.class)
    public void testToDocuments() {
        Class<?> type = Object.class;
        assertEquals(3,
            (new RepositoryOperations(type, new NitriteBuilderTest.CustomNitriteMapper(), mock(NitriteCollection.class)))
                .toDocuments(new Object[]{"42", "42", "42"}).length);
    }

    @Test
    public void testToDocuments2() {
        assertNull((new RepositoryOperations(Object.class, null, mock(NitriteCollection.class))).toDocuments(null));
    }

    @Test
    public void testToDocuments3() {
        assertNull((new RepositoryOperations(Object.class, null, mock(NitriteCollection.class)))
            .toDocuments(new Object[]{}));
    }

    @Test(expected = ObjectMappingException.class)
    public void testToDocument() {
        Class<?> type = Object.class;
        assertNull(
            (new RepositoryOperations(type, new NitriteBuilderTest.CustomNitriteMapper(), mock(NitriteCollection.class)))
                .<Object>toDocument("Object", true));
    }

    @Test
    public void testCreateUniqueFilter() {
        Class<?> type = Object.class;
        assertThrows(NotIdentifiableException.class, () -> (new RepositoryOperations(type,
            new NitriteBuilderTest.CustomNitriteMapper(), mock(NitriteCollection.class))).createUniqueFilter("Object"));
    }

    @Test
    public void testCreateIdFilter() {
        Class<?> type = Object.class;
        assertThrows(NotIdentifiableException.class, () -> (new RepositoryOperations(type,
            new NitriteBuilderTest.CustomNitriteMapper(), mock(NitriteCollection.class))).<Object>createIdFilter("Id"));
    }

    @Test
    public void testFind() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        when(nitriteCollection.find(any(), any()))
            .thenReturn(new DocumentStream(recordStream, new ProcessorChain()));
        Class<?> type = Object.class;
        RepositoryOperations repositoryOperations = new RepositoryOperations(type,
            new NitriteBuilderTest.CustomNitriteMapper(), nitriteCollection);
        Filter filter = mock(Filter.class);
        FindOptions findOptions = new FindOptions();
        assertNull(repositoryOperations.find(filter, findOptions, Object.class).getFindPlan());
        verify(nitriteCollection).find(any(), any());
    }
}

