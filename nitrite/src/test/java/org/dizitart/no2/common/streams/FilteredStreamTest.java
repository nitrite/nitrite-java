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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.Filter;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class FilteredStreamTest {

    @Test
    public void testIterator() {
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        Pair<NitriteId, Document> pair = new Pair<>();
        Pair<NitriteId, Document> pair1 = new Pair<>();
        when(recordStream.iterator()).thenReturn(
            new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{pair, pair1, new Pair<NitriteId, Document>()}));
        FilteredStream filteredStream = new FilteredStream(recordStream, mock(Filter.class));
        filteredStream.iterator();
        verify(recordStream).iterator();
        assertTrue(filteredStream.toList().isEmpty());
    }
}

