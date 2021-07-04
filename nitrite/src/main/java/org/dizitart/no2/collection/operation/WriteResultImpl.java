/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

import lombok.ToString;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.WriteResult;

import java.util.*;

/**
 * @author Anindya Chatterjee
 */
@ToString
class WriteResultImpl implements WriteResult {
    private List<NitriteId> nitriteIds;

    void setNitriteIds(List<NitriteId> nitriteIds) {
        this.nitriteIds = nitriteIds;
    }

    void addToList(NitriteId nitriteId) {
        if (nitriteIds == null) {
            nitriteIds = new ArrayList<>();
        }
        nitriteIds.add(nitriteId);
    }

    @Override
    public Iterator<NitriteId> iterator() {
        return nitriteIds == null ? Collections.emptyIterator()
            : nitriteIds.iterator();
    }
}
