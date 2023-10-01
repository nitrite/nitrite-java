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

package org.dizitart.no2.store.events;

/**
 * An enumeration of events that can occur in a Nitrite store.
 * 
 * @author Anindya Chatterjee
 * @since 4.0
 */
public enum StoreEvents {
    /**
     * Event emitted when a Nitrite database is opened.
     */
    Opened,

    /**
     * Event emitted when a commit is made to the database.
     */
    Commit,

    /**
     * Event emitted when a Nitrite database is about to close.
     */
    Closing,

    /**
     * Event emitted when a Nitrite database is closed.
     */
    Closed
}
