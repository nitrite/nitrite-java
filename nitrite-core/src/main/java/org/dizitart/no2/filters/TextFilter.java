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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.TextIndexer;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
class TextFilter extends StringFilter {
    TextFilter(String field, String value) {
        super(field, value);
    }

    @Override
    protected Set<NitriteId> findIndexedIdSet() {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getIsFieldIndexed()) {
            if (getIndexer() instanceof TextIndexer) {
                TextIndexer textIndexer = (TextIndexer) getIndexer();
                idSet = textIndexer.findText(getCollectionName(), getField(), getStringValue());
            } else {
                throw new FilterException(getField() + " is not full-text indexed");
            }
        }
        return idSet;
    }

    @Override
    protected boolean applyNonIndexed(KeyValuePair<NitriteId, Document> element) {
        throw new FilterException(getField() + " is not full-text indexed");
    }
}
