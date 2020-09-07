/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.RecordStream;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
class UnionStreamIterator implements Iterator<Pair<NitriteId, Document>> {
    private final Iterator<Pair<NitriteId, Document>> lhsIterator;
    private final Iterator<Pair<NitriteId, Document>> rhsIterator;
    private final Set<NitriteId> nitriteIds = new HashSet<>();
    private Pair<NitriteId, Document> nextItem;
    private boolean nextItemSet = false;

    public UnionStreamIterator(RecordStream<Pair<NitriteId, Document>> lhsStream,
                               RecordStream<Pair<NitriteId, Document>> rhsStream) {
        lhsIterator = lhsStream.iterator();
        rhsIterator = rhsStream.iterator();
    }

    @Override
    public boolean hasNext() {
        return nextItemSet || setNextEntry();
    }

    @Override
    public Pair<NitriteId, Document> next() {
        if (!nextItemSet && !setNextEntry()) {
            throw new NoSuchElementException();
        }
        nextItemSet = false;
        return nextItem;
    }

    private boolean setNextEntry() {
        while (lhsIterator.hasNext() || rhsIterator.hasNext()) {
            if (lhsIterator.hasNext()) {
                Pair<NitriteId, Document> pair = lhsIterator.next();
                nitriteIds.add(pair.getFirst());
                nextItem = pair;
                nextItemSet = true;
                return true;
            }

            Pair<NitriteId, Document> pair = rhsIterator.next();
            if (!nitriteIds.contains(pair.getFirst())) {
                nextItem = pair;
                nextItemSet = true;
                return true;
            }
        }
        return false;
    }
}