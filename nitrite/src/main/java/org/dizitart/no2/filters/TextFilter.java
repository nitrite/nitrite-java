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

import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.IE_TEXT_FILTER_FIELD_NOT_INDEXED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

@ToString
class TextFilter extends StringFilter {
    TextFilter(String field, String value) {
        super(field, value);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        if (indexedQueryTemplate.hasIndex(field)
                && !indexedQueryTemplate.isIndexing(field)) {
            TextIndexer textIndexer = indexedQueryTemplate.getTextIndexer();
            return textIndexer.findText(field, value);
        } else {
            throw new IndexingException(errorMessage(field + " is not indexed",
                    IE_TEXT_FILTER_FIELD_NOT_INDEXED));
        }
    }
}
