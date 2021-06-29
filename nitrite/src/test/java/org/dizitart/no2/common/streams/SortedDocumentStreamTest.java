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
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.tuples.Pair;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class SortedDocumentStreamTest {
    @Test
    public void testConstructor() {
        FindPlan findPlan = new FindPlan();
        new SortedDocumentStream(findPlan, (RecordStream<Pair<NitriteId, Document>>) mock(RecordStream.class));
        List<Pair<String, SortOrder>> blockingSortOrder = findPlan.getBlockingSortOrder();
        assertTrue(blockingSortOrder instanceof java.util.ArrayList);
        assertEquals("FindPlan(byIdFilter=null, indexScanFilter=null, collectionScanFilter=null, indexDescriptor=null,"
            + " indexScanOrder=null, blockingSortOrder=[], skip=null, limit=null, collator=null, subPlans=[])", findPlan.toString());
        assertTrue(blockingSortOrder.isEmpty());
        List<FindPlan> subPlans = findPlan.getSubPlans();
        assertTrue(subPlans instanceof java.util.ArrayList);
        assertNull(findPlan.getSkip());
        assertTrue(subPlans.isEmpty());
        assertNull(findPlan.getLimit());
        assertNull(findPlan.getIndexScanOrder());
        assertNull(findPlan.getIndexScanFilter());
        assertNull(findPlan.getIndexDescriptor());
        assertNull(findPlan.getCollectionScanFilter());
        assertNull(findPlan.getCollator());
    }

    @Test
    public void testIterator() {
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        Pair<NitriteId, Document> pair = new Pair<>();
        Pair<NitriteId, Document> pair1 = new Pair<>();
        when(recordStream.iterator()).thenReturn(
            new ArrayIterator<Pair<NitriteId, Document>>(new Pair[]{pair, pair1, new Pair<NitriteId, Document>()}));
        SortedDocumentStream sortedDocumentStream = new SortedDocumentStream(new FindPlan(), recordStream);
        sortedDocumentStream.iterator();
        verify(recordStream).iterator();
        assertTrue(sortedDocumentStream.toList().isEmpty());
    }
}

