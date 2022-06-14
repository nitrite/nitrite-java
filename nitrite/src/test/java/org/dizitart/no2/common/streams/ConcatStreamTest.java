/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
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

package org.dizitart.no2.common.streams;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class ConcatStreamTest {
    @Test
    public void testConstructor() {
        assertTrue((new ConcatStream(new ArrayList<>())).toList().isEmpty());
    }

    @Test
    public void testIterator() {
        ConcatStream concatStream = new ConcatStream(new ArrayList<>());
        concatStream.iterator();
        assertTrue(concatStream.toList().isEmpty());
    }
}

