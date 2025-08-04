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

package org.dizitart.no2.common.event;

import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.InvalidOperationException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public abstract class NitriteEventBus<EventInfo, EventListener>
    implements EventBus<EventInfo, EventListener> {

    private final Map<String, EventListener> listeners;
    private ExecutorService eventExecutor;

    public NitriteEventBus() {
        this.listeners = new ConcurrentHashMap<>();
    }

    @Override
    public String register(EventListener eventListener) {
        if (eventListener != null) {
            String subscriptionId = UUID.randomUUID().toString();
            listeners.put(subscriptionId, eventListener);
            return subscriptionId;
        }
        throw new InvalidOperationException("event listener cannot be null");
    }

    @Override
    public void deregister(String subscription) {
        if (!StringUtils.isNullOrEmpty(subscription)) {
            listeners.remove(subscription);
        }
    }

    @Override
    public void close() {
        listeners.clear();
        if (eventExecutor != null) {
            eventExecutor.shutdown();
        }
    }

    protected ExecutorService getEventExecutor() {
        if (eventExecutor == null
            || eventExecutor.isShutdown()
            || eventExecutor.isTerminated()) {
            eventExecutor = ThreadPoolManager.workerPool();
        }
        return eventExecutor;
    }

    protected Set<EventListener> getListeners() {
        return new HashSet<>(listeners.values());
    }
}
