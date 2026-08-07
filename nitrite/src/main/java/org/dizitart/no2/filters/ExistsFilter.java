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
import org.dizitart.no2.common.tuples.Pair;

/**
 * A filter that matches documents where the field is present, irrespective of
 * its value. A field explicitly set to {@code null} is present and matches.
 * <p>
 * This is a collection scanning filter, it deliberately does not extend
 * {@link ComparableFilter}. A missing field and a field holding {@code null}
 * are both stored as a null key in an index, so an index scan cannot tell them
 * apart, and electing this filter for one would give a different answer than a
 * collection scan.
 *
 * @author Anindya Chatterjee
 * @since 4.5.0
 */
class ExistsFilter extends FieldBasedFilter {
    protected ExistsFilter(String field) {
        super(field, null);
    }

    @Override
    public boolean apply(Pair<NitriteId, Document> element) {
        return element.getSecond().containsField(getField());
    }

    @Override
    public String toString() {
        return "(" + getField() + " exists)";
    }
}
