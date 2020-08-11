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

package org.dizitart.no2;

import org.dizitart.no2.collection.events.EventType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class EventTypeTest {

    @Test
    public void testValueOf() {
        assertEquals(EventType.valueOf("Insert"), EventType.Insert);
        assertEquals(EventType.valueOf("Update"), EventType.Update);
        assertEquals(EventType.valueOf("Remove"), EventType.Remove);
        assertEquals(EventType.valueOf("IndexStart"), EventType.IndexStart);
        assertEquals(EventType.valueOf("IndexEnd"), EventType.IndexEnd);

        assertEquals(EventType.values().length, 5);
    }
}
