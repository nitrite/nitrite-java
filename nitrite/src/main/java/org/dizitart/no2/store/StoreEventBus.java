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

package org.dizitart.no2.store;

import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.store.events.EventInfo;
import org.dizitart.no2.store.events.StoreEventListener;

/**
 * @since 4.0.0
 * @author Anindya Chatterjee.
 */
class StoreEventBus extends NitriteEventBus<EventInfo, StoreEventListener> {
    @Override
    public void post(EventInfo storeEvent) {
        for (final StoreEventListener listener : getListeners()) {
            getEventExecutor().submit(() -> listener.onEvent(storeEvent));
        }
    }
}
