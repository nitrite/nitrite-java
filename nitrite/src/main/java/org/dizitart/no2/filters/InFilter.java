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

import lombok.Getter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
class InFilter extends IndexAwareFilter {
    @Getter
    private Set<Comparable<?>> comparableSet;

    InFilter(String field, Comparable<?>... values) {
        super(field, values);
        this.comparableSet = new HashSet<>();
        Collections.addAll(this.comparableSet, values);
    }

    @Override
    protected Set<NitriteId> findIndexedIdSet() {
        validateInFilterValue(getField(), comparableSet);
        this.comparableSet = convertValues(this.comparableSet);

        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getIsFieldIndexed()) {
            if (getNitriteIndexer() instanceof ComparableIndexer && comparableSet != null) {
                ComparableIndexer comparableIndexer = (ComparableIndexer) getNitriteIndexer();
                idSet = comparableIndexer.findIn(getCollectionName(), getField(), comparableSet);
            } else {
                if (comparableSet != null && !comparableSet.isEmpty()) {
                    throw new FilterException("in filter is not supported on indexed field "
                        + getField());
                } else {
                    throw new FilterException("invalid in filter");
                }
            }
        }
        return idSet;
    }

    @Override
    protected Set<NitriteId> findIdSet(NitriteMap<NitriteId, Document> collection) {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getOnIdField()) {
            for (Comparable<?> comparable : comparableSet) {
                if (comparable instanceof String) {
                    NitriteId nitriteId = NitriteId.createId((String) comparable);
                    if (collection.containsKey(nitriteId)) {
                        idSet.add(nitriteId);
                    }
                }
            }
        }
        return idSet;
    }

    @Override
    public boolean apply(Pair<NitriteId, Document> element) {
        Document document = element.getSecond();
        Object fieldValue = document.get(getField());

        if (fieldValue instanceof Comparable) {
            Comparable<?> comparable = (Comparable<?>) fieldValue;
            return comparableSet.contains(comparable);
        }
        return false;
    }

    private void validateInFilterValue(String field, Collection<Comparable<?>> values) {
        notNull(field, "field cannot be null");
        notNull(values, "values cannot be null");
        if (values.size() == 0) {
            throw new ValidationException("values cannot be empty");
        }
    }
}
