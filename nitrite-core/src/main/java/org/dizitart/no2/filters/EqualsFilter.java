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

import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.index.TextIndexer;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;

/**
 * @author Anindya Chatterjee.
 */
@ToString
class EqualsFilter extends IndexAwareFilter {
    EqualsFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected Set<NitriteId> findIndexedIdSet() {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getIsFieldIndexed()) {
            if (getValue() == null || getValue() instanceof Comparable) {
                if (getIndexer() instanceof ComparableIndexer) {
                    ComparableIndexer comparableIndexer = (ComparableIndexer) getIndexer();
                    idSet = comparableIndexer.findEqual(getCollectionName(), getField(), (Comparable) getValue());
                } else if (getIndexer() instanceof TextIndexer && getValue() instanceof String) {
                    // eq filter is not compatible with TextIndexer
                    setIsFieldIndexed(false);
                } else {
                    throw new FilterException("eq filter is not supported on indexed field "
                        + getField());
                }
            } else {
                throw new FilterException(getValue() + " is not comparable");
            }
        }
        return idSet;
    }

    @Override
    public boolean applyNonIndexed(KeyValuePair<NitriteId, Document> element) {
        Object value = getValue();

        if (getField().equalsIgnoreCase(DOC_ID)) {
            if (value instanceof String) {
                return Objects.equals(element.getKey().getIdValue(), getValue());
            }
        } else {
            Document document = element.getValue();
            Object fieldValue = document.get(getField());
            return deepEquals(fieldValue, value);
        }

        return false;
    }
}
