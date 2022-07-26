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

import com.fasterxml.jackson.databind.util.ArrayIterator;
import org.dizitart.no2.NitriteBuilderTest;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.mapper.MappableMapper;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.common.streams.DocumentStream;
import org.dizitart.no2.common.streams.MutatedObjectStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ObjectCursorTest {
    @Test
    public void testSize() {
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        Pair<NitriteId, Document> pair = new Pair<>();
        Pair<NitriteId, Document> pair1 = new Pair<>();
        when(recordStream.iterator()).thenReturn(
            new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{pair, pair1, new Pair<NitriteId, Document>()}));
        DocumentStream cursor = new DocumentStream(recordStream, new ProcessorChain());
        NitriteBuilderTest.CustomNitriteMapper nitriteMapper = new NitriteBuilderTest.CustomNitriteMapper();
        ObjectCursor<Object> objectCursor = new ObjectCursor<>(nitriteMapper, cursor, Object.class);
        assertEquals(3L, objectCursor.size());
        verify(recordStream).iterator();
        assertTrue(objectCursor.toList().isEmpty());
    }

    @Test
    public void testConstructor() {
        NitriteBuilderTest.CustomNitriteMapper nitriteMapper = new NitriteBuilderTest.CustomNitriteMapper();
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        DocumentStream cursor = new DocumentStream(recordStream, new ProcessorChain());
        assertNull((new ObjectCursor<>(nitriteMapper, cursor, Object.class)).getFindPlan());
    }

    @Test
    public void testGetFindPlan() {
        NitriteBuilderTest.CustomNitriteMapper nitriteMapper = new NitriteBuilderTest.CustomNitriteMapper();
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        DocumentStream cursor = new DocumentStream(recordStream, new ProcessorChain());
        assertNull((new ObjectCursor<>(nitriteMapper, cursor, Object.class)).getFindPlan());
    }

    @Test
    public void testProject() {
        NitriteBuilderTest.CustomNitriteMapper nitriteMapper = new NitriteBuilderTest.CustomNitriteMapper();
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        DocumentStream cursor = new DocumentStream(recordStream, new ProcessorChain());
        ObjectCursor<Object> objectCursor = new ObjectCursor<>(nitriteMapper, cursor, Object.class);
        assertThrows(ValidationException.class, () -> objectCursor.project(Object.class));
    }

    @Test
    public void testProject3() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        MappableMapper nitriteMapper = new MappableMapper(forNameResult, forNameResult1, Object.class);
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        DocumentStream cursor = new DocumentStream(recordStream, new ProcessorChain());
        ObjectCursor<Object> objectCursor = new ObjectCursor<>(nitriteMapper, cursor, Object.class);
        assertThrows(ValidationException.class, () -> objectCursor.project(Object.class));
    }

    @Test
    public void testJoin() {
        NitriteBuilderTest.CustomNitriteMapper nitriteMapper = new NitriteBuilderTest.CustomNitriteMapper();
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        DocumentStream cursor = new DocumentStream(recordStream, new ProcessorChain());
        ObjectCursor<Object> objectCursor = new ObjectCursor<>(nitriteMapper, cursor, Object.class);
        NitriteBuilderTest.CustomNitriteMapper nitriteMapper1 = new NitriteBuilderTest.CustomNitriteMapper();
        RecordStream<Pair<NitriteId, Document>> recordStream1 = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        DocumentStream cursor1 = new DocumentStream(recordStream1, new ProcessorChain());
        ObjectCursor<Object> foreignCursor = new ObjectCursor<>(nitriteMapper1, cursor1, Object.class);
        Lookup lookup = new Lookup();
        assertTrue(objectCursor.join(foreignCursor, lookup, Object.class) instanceof MutatedObjectStream);
    }
}

