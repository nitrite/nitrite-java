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

package org.dizitart.no2.common.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.common.util.Iterables.firstOrNull;
import static org.dizitart.no2.common.util.Iterables.toArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Anindya Chatterjee.
 */
public class IterablesTest {
    @Test
    public void testFirstOrDefault() {
        assertNull(firstOrNull(new ArrayList<>()));
        assertNull(firstOrNull(null));
    }

    @Test
    public void testToArray() {
        final List<String> list = new ArrayList<String>() {{
            add("a");
            add("b");
        }};
        assertArrayEquals(toArray(new ArrayList<String>() {{
                add("a");
                add("b");
            }}),
            new String[]{"a", "b"});
        assertArrayEquals(toArray(list), new String[]{"a", "b"});
    }
}
