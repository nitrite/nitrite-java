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

package org.dizitart.no2.common.meta;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class AttributesTest {
    @Test
    public void testConstructor() {
        Map<String, String> attributes = (new Attributes()).getAttributes();
        assertTrue(attributes instanceof java.util.concurrent.ConcurrentHashMap);
        assertEquals(3, attributes.size());
    }

    @Test
    public void testConstructor2() {
        Map<String, String> attributes = (new Attributes("collection")).getAttributes();
        assertTrue(attributes instanceof java.util.concurrent.ConcurrentHashMap);
        assertEquals(4, attributes.size());
    }

    @Test
    public void testSet() {
        Attributes attributes = new Attributes();
        assertSame(attributes, attributes.set("key", "value"));
    }

    @Test
    public void testGet() {
        assertNull((new Attributes()).get("key"));
    }

    @Test
    public void testHasKey() {
        assertFalse((new Attributes()).hasKey("key"));
    }
}

