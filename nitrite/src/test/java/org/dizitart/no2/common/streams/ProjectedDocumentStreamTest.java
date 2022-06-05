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

package org.dizitart.no2.common.streams;

import com.fasterxml.jackson.databind.util.ArrayIterator;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.common.tuples.Pair;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class ProjectedDocumentStreamTest {
    @Test
    public void testConstructor() {
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        new ProjectedDocumentStream(recordStream, null, new ProcessorChain());
        assertNull(null);
    }

    @Test
    public void testIterator() {
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        Pair<NitriteId, Document> pair = new Pair<>();
        Pair<NitriteId, Document> pair1 = new Pair<>();
        when(recordStream.iterator()).thenReturn(
            new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{pair, pair1, new Pair<NitriteId, Document>()}));
        ProjectedDocumentStream projectedDocumentStream = new ProjectedDocumentStream(recordStream, null,
            new ProcessorChain());
        projectedDocumentStream.iterator();
        verify(recordStream).iterator();
        assertTrue(projectedDocumentStream.toList().isEmpty());
    }

    @Test
    public void testIterator2() {
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        when(recordStream.iterator()).thenReturn(new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{}));
        ProjectedDocumentStream projectedDocumentStream = new ProjectedDocumentStream(recordStream, null,
            new ProcessorChain());
        projectedDocumentStream.iterator();
        verify(recordStream).iterator();
        assertTrue(projectedDocumentStream.toList().isEmpty());
    }
    @Test
    public void test() {
        try(Nitrite db = Nitrite.builder().openOrCreate()) {
            Document document = Document.createDocument("name", "John")
                .put("address", Document.createDocument("street", "Main Street")
                    .put("city", "New York")
                    .put("state", "NY")
                    .put("zip", "10001"));
            db.getCollection("users").insert(document);

            Document projection = Document.createDocument("name", null)
                .put("address.city", null)
                .put("address.state", null);

            db.getCollection("users")
                .find()
                .project(projection)
                .forEach(System.out::println);
        }
    }
}

