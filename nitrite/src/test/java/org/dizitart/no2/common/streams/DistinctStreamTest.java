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
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DistinctStreamTest {
    @Test
    public void testConstructor() {
        DistinctStream distinctStream = new DistinctStream(null);
        assertFalse(distinctStream.iterator().hasNext());
    }

    @Test
    public void testConstructor2() {
        RecordStream<Pair<NitriteId, Document>> recordStream = () -> new Iterator<Pair<NitriteId, Document>>() {
            int i = 0;

            // same id for all entries
            final NitriteId id = NitriteId.newId();

            @Override
            public boolean hasNext() {
                return i != 2;
            }

            @Override
            public Pair<NitriteId, Document> next() {
                i++;
                return Pair.pair(id, Document.createDocument("value", i));
            }
        };

        assertEquals(2, recordStream.size());

        DistinctStream distinctStream = new DistinctStream(recordStream);
        assertEquals(1, distinctStream.size());
    }
}

