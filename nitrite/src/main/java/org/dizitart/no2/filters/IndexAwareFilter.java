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

package org.dizitart.no2.filters;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@ToString(exclude = "indexedIdSet")
@EqualsAndHashCode(callSuper = true)
public abstract class IndexAwareFilter extends FieldBasedFilter {
    @Getter @Setter
    private Boolean isFieldIndexed = false;

    @Getter @Setter
    private Boolean onIdField = false;

    @Getter @Setter
    private NitriteIndexer nitriteIndexer;

    private Set<NitriteId> indexedIdSet;
    private Set<NitriteId> idSet;

    protected IndexAwareFilter(String field, Object value) {
        super(field, value);
    }

    protected abstract Set<NitriteId> findIndexedIdSet();
    protected abstract Set<NitriteId> findIdSet(NitriteMap<NitriteId, Document> collection);

    public Set<NitriteId> cachedIndexedIds() {
        if (indexedIdSet == null || indexedIdSet.isEmpty()) {
            indexedIdSet = findIndexedIdSet();
        }
        return indexedIdSet;
    }

    public Set<NitriteId> cachedIds(NitriteMap<NitriteId, Document> collection) {
        if (idSet == null) {
            idSet = findIdSet(collection);
        }
        return idSet;
    }
}
