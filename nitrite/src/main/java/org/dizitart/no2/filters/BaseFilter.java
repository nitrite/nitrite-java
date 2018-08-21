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

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Filter;
import org.dizitart.no2.index.IndexedQueryTemplate;
import org.dizitart.no2.store.NitriteMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * An abstract implementation of {@link Filter}.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Slf4j
public abstract class BaseFilter implements Filter {
    /**
     * The {@link IndexedQueryTemplate}.
     */
    protected IndexedQueryTemplate indexedQueryTemplate;

    @Override
    public void setIndexedQueryTemplate(IndexedQueryTemplate indexedQueryTemplate) {
        this.indexedQueryTemplate = indexedQueryTemplate;
    }

    List<Callable<Set<NitriteId>>> createTasks(Filter[] filters,
                                               final NitriteMap<NitriteId, Document> documentMap) {
        List<Callable<Set<NitriteId>>> tasks = new ArrayList<>();
        for (final Filter filter : filters) {
            filter.setIndexedQueryTemplate(indexedQueryTemplate);
            tasks.add(() -> {
                try {
                    return filter.apply(documentMap);
                } catch (Exception e) {
                    log.error("Error while executing filter " + filter.toString(), e);
                    throw e;
                }
            });
        }
        return tasks;
    }
}
