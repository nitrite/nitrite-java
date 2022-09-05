/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

package org.dizitart.no2.index;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.util.StringUtils;

import java.util.Arrays;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * Represents an index fields.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@EqualsAndHashCode(callSuper = true)
public class IndexFields extends Fields {
    private static final long serialVersionUID = 1662219840L;

    /**
     * The index type.
     */
    @Getter
    private String indexType;

    /**
     * Creates a {@link IndexFields} instance with field names and index type.
     *
     * @param indexType the index type
     * @param fields    the fields
     * @return the fields
     */
    public static IndexFields create(String indexType, String... fields) {
        notNull(fields, "fields cannot be null");
        notEmpty(fields, "fields cannot be empty");

        if (StringUtils.isNullOrEmpty(indexType)) {
            indexType = IndexType.UNIQUE;
        }

        IndexFields f = new IndexFields();
        f.fieldNames.addAll(Arrays.asList(fields));
        f.indexType = indexType;
        return f;
    }

    @Override
    public String getEncodedName() {
        return indexType + "[" + super.getEncodedName() + "]";
    }

    @Override
    public String toString() {
        return this.getEncodedName();
    }
}
