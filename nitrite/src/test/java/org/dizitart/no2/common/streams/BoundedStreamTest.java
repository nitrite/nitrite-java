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
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class BoundedStreamTest {
    @Test
    public void testConstructor() {
        new BoundedStream<>(1L, 1L, (RecordStream<Pair<NitriteId, Document>>) mock(RecordStream.class));
    }

    @Test
    public void testConstructor2() {
        assertThrows(ValidationException.class,
                () -> new BoundedStream<>(-1L, 1L, (RecordStream<Pair<NitriteId, Document>>) mock(RecordStream.class)));
    }

    @Test
    public void testConstructor3() {
        assertThrows(ValidationException.class,
                () -> new BoundedStream<>(1L, -1L, (RecordStream<Pair<NitriteId, Document>>) mock(RecordStream.class)));
    }

    @Test
    public void testIterator() {
        RecordStream<Pair<NitriteId, Document>> recordStream = () -> new Iterator<Pair<NitriteId, Document>>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i != 3;
            }

            @Override
            public Pair<NitriteId, Document> next() {
                i++;
                return Pair.pair(NitriteId.newId(), Document.createDocument("value", i));
            }
        };

        BoundedStream<NitriteId, Document> stream
            = new BoundedStream<>(2L, 1L, recordStream);

        for (Pair<NitriteId, Document> pair : stream) {
            assertEquals(3, (int) pair.getSecond().get("value", Integer.class));
        }
    }
}

