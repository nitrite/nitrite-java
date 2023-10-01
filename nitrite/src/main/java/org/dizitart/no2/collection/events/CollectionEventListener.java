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

package org.dizitart.no2.collection.events;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;

/**
 * A listener which is able to listen to any changes in a
 * in a {@link NitriteCollection} or {@link ObjectRepository}.
 *
 * @author Anindya Chatterjee.
 * @since 4.0
 */
public interface CollectionEventListener {

    /**
     * A subscriber to listen to collection events.
     *
     * @param eventInfo the event information
     */
    void onEvent(CollectionEventInfo<?> eventInfo);
}
