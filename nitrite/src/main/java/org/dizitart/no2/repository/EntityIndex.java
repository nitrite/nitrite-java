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

package org.dizitart.no2.repository;

import lombok.Getter;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.index.IndexType;

import java.util.Arrays;
import java.util.List;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * Represents an index for an entity in the Nitrite database.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class EntityIndex {

    @Getter
    /**
     * The type of index to be used for the entity field.
     */
    private String indexType;

    @Getter
    /**
     * The list of field names on which index is created.
     */
    private List<String> fieldNames;

    public EntityIndex(String indexType, String... fields) {
        notNull(fields, "fields cannot be null");
        notEmpty(fields, "fields cannot be empty");

        if (StringUtils.isNullOrEmpty(indexType)) {
            indexType = IndexType.UNIQUE;
        }

        this.fieldNames = Arrays.asList(fields);
        this.indexType = StringUtils.isNullOrEmpty(indexType) ? IndexType.UNIQUE : indexType;
    }
}
