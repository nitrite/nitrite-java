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

package org.dizitart.no2.index;

import org.dizitart.no2.NitriteId;

/**
 * @author Anindya Chatterjee
 */
public interface Indexer<T> {

    void writeIndex(NitriteId id, String field, T element, boolean unique);

    void updateIndex(NitriteId id, String field, T newElement, T oldElement, boolean unique);

    void removeIndex(NitriteId id, String field, T element);

    void dropIndex(String field);

    void rebuildIndex(String field, boolean unique);
}
