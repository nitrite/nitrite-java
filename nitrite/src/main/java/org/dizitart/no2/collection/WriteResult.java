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

package org.dizitart.no2.collection;

import org.dizitart.no2.NitriteId;

/**
 * An interface to represent the result of a modification operation
 * on {@link NitriteCollection}. It provides a means to iterate over
 * all affected ids in the collection.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public interface WriteResult extends Iterable<NitriteId> {

    /**
     * Gets the count of affected document in the collection.
     *
     * @return the affected document count.
     */
    int getAffectedCount();
}
