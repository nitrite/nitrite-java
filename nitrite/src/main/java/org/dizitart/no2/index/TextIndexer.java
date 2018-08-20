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

package org.dizitart.no2.index;

import org.dizitart.no2.NitriteId;
import org.dizitart.no2.filters.ObjectFilters;

import java.util.Set;

/**
 * Represents a full-text indexing engine. It scans a document
 * and modifies full-text index entries by decomposing texts of
 * an indexed field, into a set of string tokens. It uses the
 * full-text index to search for a specific text.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 * @see org.dizitart.no2.NitriteBuilder#textIndexer(TextIndexer)
 * @see org.dizitart.no2.filters.Filters#text(String, String)
 * @see ObjectFilters#text(String, String)
 */
public interface TextIndexer extends Indexer<String> {

    /**
     * Finds with text filer using full-text index.
     *
     * @param field the value
     * @param value the value
     * @return the result set
     */
    Set<NitriteId> findText(String field, String value);
}
