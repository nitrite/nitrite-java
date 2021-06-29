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

package org.dizitart.no2.index;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.index.fulltext.TextTokenizer;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a nitrite text indexer.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class NitriteTextIndexer implements NitriteIndexer {
    private final TextTokenizer textTokenizer;
    private final Map<IndexDescriptor, TextIndex> indexRegistry;

    /**
     * Instantiates a new {@link NitriteTextIndexer}.
     */
    public NitriteTextIndexer() {
        this.textTokenizer = new EnglishTextTokenizer();
        this.indexRegistry = new ConcurrentHashMap<>();
    }

    /**
     * Instantiates a new {@link NitriteTextIndexer}.
     *
     * @param textTokenizer the text tokenizer
     */
    public NitriteTextIndexer(TextTokenizer textTokenizer) {
        this.textTokenizer = textTokenizer;
        this.indexRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
    }

    @Override
    public String getIndexType() {
        return IndexType.FULL_TEXT;
    }

    @Override
    public void validateIndex(Fields fields) {
        if (fields.getFieldNames().size() > 1) {
            throw new IndexingException("text index can only be created on a single field");
        }
    }

    @Override
    public void dropIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        TextIndex textIndex = findTextIndex(indexDescriptor, nitriteConfig);
        textIndex.drop();
    }

    @Override
    public void writeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        TextIndex textIndex = findTextIndex(indexDescriptor, nitriteConfig);
        textIndex.write(fieldValues);
    }

    @Override
    public void removeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        TextIndex textIndex = findTextIndex(indexDescriptor, nitriteConfig);
        textIndex.remove(fieldValues);
    }

    @Override
    public LinkedHashSet<NitriteId> findByFilter(FindPlan findPlan, NitriteConfig nitriteConfig) {
        TextIndex textIndex = findTextIndex(findPlan.getIndexDescriptor(), nitriteConfig);
        return textIndex.findNitriteIds(findPlan);
    }

    private TextIndex findTextIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        if (indexRegistry.containsKey(indexDescriptor)) {
            return indexRegistry.get(indexDescriptor);
        }

        TextIndex textIndex = new TextIndex(textTokenizer, indexDescriptor, nitriteConfig.getNitriteStore());
        indexRegistry.put(indexDescriptor, textIndex);
        return textIndex;
    }
}
