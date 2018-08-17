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

import org.dizitart.no2.common.ExecutorServiceManager;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * An abstract implementation of {@link EventBus}.
 *
 * @param <EventInfo>     the event information type parameter
 * @param <EventListener> the event listener type parameter
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public abstract class NitriteEventBus<EventInfo, EventListener>
        implements EventBus<EventInfo, EventListener> {

    private Set<EventListener> listeners;
    private ExecutorService eventExecutor;

    /**
     * Instantiates a new Nitrite event bus.
     *
     */
    public NitriteEventBus() {
        this.listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public void register(EventListener eventListener) {
        if (eventListener != null) {
            listeners.add(eventListener);
        }
    }

    @Override
    public void deregister(EventListener eventListener) {
        if (eventListener != null) {
            listeners.remove(eventListener);
        }
    }

    @Override
    public void close() {
        listeners.clear();
    }

    /**
     * Gets the {@link ExecutorService} that executes listeners' code.
     *
     * @return the {@link ExecutorService}.
     */
    protected ExecutorService getEventExecutor() {
        if (eventExecutor == null) {
            eventExecutor = ExecutorServiceManager.daemonExecutor();
        }
        return eventExecutor;
    }

    /**
     * Gets a set of all event listeners.
     *
     * @return the event listeners
     */
    protected Set<EventListener> getListeners() {
        return listeners;
    }
}
