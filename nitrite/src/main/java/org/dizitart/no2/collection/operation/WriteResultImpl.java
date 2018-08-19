/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.operation;

import lombok.ToString;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.WriteResult;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
@ToString
class WriteResultImpl implements WriteResult {
    private List<NitriteId> nitriteIdList;

    void setNitriteIdList(List<NitriteId> nitriteIdList) {
        this.nitriteIdList = nitriteIdList;
    }

    void addToList(NitriteId nitriteId) {
        if (nitriteIdList == null) {
            nitriteIdList = new ArrayList<>();
        }
        nitriteIdList.add(nitriteId);
    }

    public int getAffectedCount() {
        if (nitriteIdList == null) return 0;
        return nitriteIdList.size();
    }

    @NotNull
    @Override
    public Iterator<NitriteId> iterator() {
        return nitriteIdList.iterator();
    }
}
