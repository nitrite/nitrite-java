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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.filters.Filter;

/**
 * Settings to control update operation in {@link NitriteCollection}.
 *
 * @author Anindya Chatterjee
 * @see NitriteCollection#update(Filter, Document, UpdateOptions)
 * @since 1.0
 */
@ToString
public class UpdateOptions {

    /**
     * Indicates if the update operation will insert a new document if it
     * does not find any existing document to update.
     *
     * @param insertIfAbsent a value indicating, if a new document to insert in case the
     * filter fails to find a document to update.
     * @return `true` if a new document to insert; otherwise, `false`.
     * @see NitriteCollection#update(Filter, Document, UpdateOptions)
     */
    @Getter
    @Setter
    private boolean insertIfAbsent;

    /**
     * Indicates if only one document will be updated or all of them.
     *
     * @param justOne a value indicating if only one document to update or all.
     * @return `true` if only one document to update; otherwise, `false`.
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
