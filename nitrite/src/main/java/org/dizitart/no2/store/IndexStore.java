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

package org.dizitart.no2.store;

import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.index.Index;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Anindya Chatterjee
 */
public interface IndexStore {
    boolean hasIndex(String field);

    Index findIndex(String field);

    NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> getIndexMap(String field);

    void mark(String field, boolean dirty);

    boolean isDirtyIndex(String field);

    Collection<Index> listIndexes();

    void dropIndex(String field);

    void dropAll();

    Index createIndex(String field, IndexType indexType);
}
