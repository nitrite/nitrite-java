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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a collection event data.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionEventInfo<T> {
    /**
     * Specifies the item triggering the event.
     *
     * @param item the item that changed.
     * @returns the item.
     */
    private T item;

    /**
     * Specifies the change type.
     *
     * @param changeType the type of the change.
     * @returns the type of the change.
     */
    private EventType eventType;

    /**
     * Specifies the unix timestamp of the change.
     *
     * @param changeTimestamp the unix timestamp of the change.
     * @returns the unix timestamp of the change.
     */
    private long timestamp;

    /**
     * Specifies the name of the originator who has initiated this event.
     *
     * @param originator name of originator of the event.
     * @returns name of the originator.
     * @since 4.0.0
     */
    private String originator;

    public CollectionEventInfo(EventType eventType) {
        this.eventType = eventType;
    }
}
