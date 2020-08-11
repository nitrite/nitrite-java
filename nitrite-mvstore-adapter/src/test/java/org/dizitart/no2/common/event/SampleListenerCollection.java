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

import lombok.Getter;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.events.EventType;

/**
 * @author Anindya Chatterjee.
 */
@Getter
class SampleListenerCollection implements CollectionEventListener {
    private EventType action;
    private Object item;

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {
        if (eventInfo != null) {
            this.action = eventInfo.getEventType();
            this.item = eventInfo.getItem();
        }
    }
}
