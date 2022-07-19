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

package org.dizitart.no2.transaction;

/**
 * Represents a change type in a transaction.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
enum ChangeType {
    /**
     * Insert
     */
    Insert,

    /**
     * Update.
     */
    Update,

    /**
     * Remove.
     */
    Remove,

    /**
     * Clear. Commit only operation, cannot be rolled back.
     */
    Clear,

    /**
     * Create index.
     */
    CreateIndex,

    /**
     * Rebuild index.
     */
    RebuildIndex,

    /**
     * Drop index.
     */
    DropIndex,

    /**
     * Drop all indices.
     */
    DropAllIndexes,

    /**
     * Drop collection. Commit only operation, cannot be rolled back.
     */
    DropCollection,

    /**
     * Set attribute.
     */
    SetAttributes,
}
