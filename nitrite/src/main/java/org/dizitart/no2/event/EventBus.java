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

package org.dizitart.no2.event;

/**
 * Represents a generic publish/subscribe event bus interface.
 *
 * @param <EventInfo>     the event information type parameter
 * @param <EventListener> the event listener type parameter
 * @author Anindya Chatterjee
 * @since 1.0
 */
public interface EventBus<EventInfo, EventListener> {
    /**
     * Registers an event listener to the event-bus.
     *
     * @param listener the event listener
     */
    void register(EventListener listener);

    /**
     * De-registers an already registered event listener.
     *
     * @param listener the event listener
     */
    void deregister(EventListener listener);

    /**
     * Posts an event to the event bus. All registered
     * event listeners for this event will receive the `eventInfo`
     * for further processing.
     * 
     * Event processing is asynchronous.
     *
     * @param eventInfo the event related information
     */
    void post(EventInfo eventInfo);

    /**
     * Closes the event bus and de-registers all event listeners.
     */
    void close();
}
