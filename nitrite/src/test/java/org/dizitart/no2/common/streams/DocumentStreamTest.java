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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
public class DocumentStreamTest {
    @Test
    public void testConstructor() {
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        assertNull((new DocumentStream(recordStream, new ProcessorChain())).getFindPlan());
    }

    @Test
    public void testJoin() {
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        DocumentStream documentStream = new DocumentStream(recordStream, new ProcessorChain());
        RecordStream<Pair<NitriteId, Document>> recordStream1 = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        DocumentStream foreignCursor = new DocumentStream(recordStream1, new ProcessorChain());
        assertTrue(documentStream.join(foreignCursor, new Lookup()) instanceof JoinedDocumentStream);
    }
}

