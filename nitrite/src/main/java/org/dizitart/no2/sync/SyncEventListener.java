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

package org.dizitart.no2.sync;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the base event listener for replication event.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Getter @Setter
public abstract class SyncEventListener {
    /**
     * Gets the source collection name.
     *
     * @param collectionName source collection name
     * @return collection name.
     * */
    private String collectionName;

    /**
     * Listener routine to be invoked for each replication event.
     *
     * @param eventInfo the replication event data
     */
    public abstract void onSyncEvent(SyncEventData eventInfo);
}
