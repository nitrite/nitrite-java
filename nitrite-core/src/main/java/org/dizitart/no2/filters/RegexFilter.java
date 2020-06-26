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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anindya Chatterjee
 */
class RegexFilter extends FieldBasedFilter {
    RegexFilter(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        String value = (String) getValue();
        Pattern pattern = Pattern.compile(value);

        Document document = element.getValue();
        Object fieldValue = document.get(getField());
        if (fieldValue != null) {
            if (fieldValue instanceof String) {
                Matcher matcher = pattern.matcher((String) fieldValue);
                if (matcher.find()) {
                    return true;
                }
                matcher.reset();
            } else {
                throw new FilterException(getField() + " does not contain string value");
            }
        }
        return false;
    }
}
