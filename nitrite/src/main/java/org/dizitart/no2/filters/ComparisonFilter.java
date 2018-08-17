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
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.collection.NitriteService;

import static org.dizitart.no2.exceptions.ErrorMessage.VALUE_IS_NOT_COMPARABLE;

@Getter
@ToString
abstract class ComparisonFilter extends BaseFilter {
    protected String field;
    protected Comparable comparable;

    ComparisonFilter(String field, Object value) {
        if (value instanceof Comparable) {
            this.comparable = (Comparable) value;
        } else {
            throw new FilterException(VALUE_IS_NOT_COMPARABLE);
        }
        this.field = field;
    }

    @Override
    public void setNitriteService(NitriteService nitriteService) {
        this.nitriteService = nitriteService;
    }
}
