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
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.Iterables;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a sorted nitrite document stream
 *
 * @since 4.0
 * @author Anindya Chatterjee.
 */
public class SortedDocumentStream implements RecordStream<Pair<NitriteId, Document>> {
    private final FindPlan findPlan;
    private final RecordStream<Pair<NitriteId, Document>> recordStream;

    public SortedDocumentStream(FindPlan findPlan,
                                RecordStream<Pair<NitriteId, Document>> recordStream) {
        this.findPlan = findPlan;
        this.recordStream = recordStream;
    }

    @Override
    public Iterator<Pair<NitriteId, Document>> iterator() {
        if (recordStream == null) return Collections.emptyIterator();

        DocumentSorter documentSorter = new DocumentSorter(findPlan.getCollator(),
            findPlan.getBlockingSortOrder());

        List<Pair<NitriteId, Document>> recordList = Iterables.toList(recordStream);
        Collections.sort(recordList, documentSorter);

        return recordList.iterator();
    }
}
