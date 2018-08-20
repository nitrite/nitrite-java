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

package org.dizitart.no2.filters;

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.exceptions.ErrorCodes.FE_LT_FIELD_NOT_COMPARABLE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.dizitart.no2.util.NumberUtils.compare;

@Getter
@ToString
class LesserThanFilter extends ComparisonFilter {
    LesserThanFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        if (field.equals(DOC_ID)) {
            Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
            NitriteId nitriteId = null;
            if (comparable instanceof Long) {
                nitriteId = NitriteId.createId((Long) comparable);
            }

            if (nitriteId != null) {
                NitriteId lowerKey = documentMap.lowerKey(nitriteId);
                while (lowerKey != null) {
                    nitriteIdSet.add(lowerKey);
                    lowerKey = documentMap.lowerKey(lowerKey);
                }
            }
            return nitriteIdSet;
        } else if (indexedQueryTemplate.hasIndex(field)
                && !indexedQueryTemplate.isIndexing(field)) {
            ComparableIndexer comparableIndexer = indexedQueryTemplate.getComparableIndexer();
            return comparableIndexer.findLesserThan(field, comparable);
        } else {
            return matchedSet(documentMap);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = getFieldValue(document, field);
            if (fieldValue != null) {
                if (fieldValue instanceof Number && comparable instanceof Number) {
                    if (compare((Number) fieldValue, (Number) comparable) < 0) {
                        nitriteIdSet.add(entry.getKey());
                    }
                } else if (fieldValue instanceof Comparable) {
                    Comparable arg = (Comparable) fieldValue;
                    if (arg.compareTo(comparable) < 0) {
                        nitriteIdSet.add(entry.getKey());
                    }
                } else {
                    throw new FilterException(errorMessage(
                            fieldValue + " is not comparable",
                            FE_LT_FIELD_NOT_COMPARABLE));
                }
            }
        }
        return nitriteIdSet;
    }
}
