/*
 *
 * Copyright 2018 Nitrite author or authors.
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

package org.dizitart.no2.sync;

/**
 * Represents different types of replication events.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public enum EventType {
    /**
     * Replication started.
     */
    STARTED,
    /**
     * Replication is in progress.
     */
    IN_PROGRESS,
    /**
     * Replication has been completed.
     */
    COMPLETED,
    /**
     * Replication has been canceled by user.
     */
    CANCELED,
    /**
     * Replication has been stopped by user.
     */
    STOPPED,
    /**
     * Replication has failed with an exception.
     */
    REPLICATION_ERROR,
}
