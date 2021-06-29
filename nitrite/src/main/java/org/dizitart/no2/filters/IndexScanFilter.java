/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.InvalidOperationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a set of filter which can be applied on an index.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@ToString
public class IndexScanFilter implements Filter {
    @Getter
    private final List<ComparableFilter> filters;

    /**
     * Instantiates a new Index scan filter.
     *
     * @param filters the filters
     */
    public IndexScanFilter(Collection<ComparableFilter> filters) {
        this.filters = new ArrayList<>(filters);
    }

    @Override
    public boolean apply(Pair<NitriteId, Document> element) {
        throw new InvalidOperationException("index scan filter cannot be applied on collection");
    }
}
