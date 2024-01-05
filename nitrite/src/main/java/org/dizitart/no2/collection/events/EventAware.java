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

/**
 * An interface to make a {@link org.dizitart.no2.collection.NitriteCollection} or {@link org.dizitart.no2.repository.ObjectRepository}
 * event aware.
 * 
 * @since 4.0
 * @author Anindya Chatterjee
 */
public interface EventAware {
    /**
     * Subscribes a listener to the collection event.
     *
     * @param listener the listener to subscribe
     */
    void subscribe(CollectionEventListener listener);

    /**
     * Unsubscribes a listener from the collection event.
     *
     * @param listener the listener to unsubscribe
     */
    void unsubscribe(CollectionEventListener listener);
}
