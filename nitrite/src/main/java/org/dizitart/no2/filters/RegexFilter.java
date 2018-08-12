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
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dizitart.no2.exceptions.ErrorCodes.FE_REGEX_NO_STRING_VALUE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;

/**
 * @author Anindya Chatterjee.
 */
@ToString
class RegexFilter extends StringFilter {
    RegexFilter(String field, String value) {
        super(field, value);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile(value);

        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = getFieldValue(document, field);
            if (fieldValue != null) {
                if (fieldValue instanceof String) {
                    Matcher matcher = pattern.matcher((String) fieldValue);
                    if (matcher.find()) {
                        nitriteIdSet.add(entry.getKey());
                    }
                    matcher.reset();
                } else {
                    throw new FilterException(errorMessage(
                            field + " does not contain string value.",
                            FE_REGEX_NO_STRING_VALUE));
                }
            }
        }
        return nitriteIdSet;
    }
}
