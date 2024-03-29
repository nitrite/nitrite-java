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

package org.dizitart.no2.collection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.filters.Filter;

/**
 * Represents options to configure update operation.
 *
 * @author Anindya Chatterjee
 * @see NitriteCollection#update(Filter, Document, UpdateOptions)
 * @since 1.0
 */
@Getter
@ToString
@EqualsAndHashCode
public class UpdateOptions {

    /**
     * Indicates if the update operation will insert a new document if it
     * does not find any existing document to update.
     *
     * @see NitriteCollection#update(Filter, Document, UpdateOptions)
     */
    @Setter
    private boolean insertIfAbsent;

    /**
     * Indicates if only one document will be updated or all of them.
     */
    @Getter
    @Setter
    private boolean justOnce;

    /**
     * Creates a new {@link UpdateOptions}.
     *
     * @param insertIfAbsent the insertIfAbsent flag
     * @return the {@link UpdateOptions}.
     */
    public static UpdateOptions updateOptions(boolean insertIfAbsent) {
        UpdateOptions options = new UpdateOptions();
        options.setInsertIfAbsent(insertIfAbsent);
        return options;
    }

    /**
     * Creates a new {@link UpdateOptions}.
     *
     * @param insertIfAbsent the insertIfAbsent flag
     * @param justOnce       the justOnce flag
     * @return the {@link UpdateOptions}.
     */
    public static UpdateOptions updateOptions(boolean insertIfAbsent, boolean justOnce) {
        UpdateOptions options = new UpdateOptions();
        options.setInsertIfAbsent(insertIfAbsent);
        options.setJustOnce(justOnce);
        return options;
    }
}
