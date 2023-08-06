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

package org.dizitart.no2.mvstore;

import org.dizitart.no2.common.tuples.Pair;
import org.h2.mvstore.MVMap;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;

public class ReverseIteratorTest {

    @Test
    public void testHasNext() {
        ReverseIterator<Object, Object> reverseIterator = (ReverseIterator<Object, Object>) mock(ReverseIterator.class);
        when(reverseIterator.hasNext()).thenReturn(true);
        reverseIterator.hasNext();
        verify(reverseIterator).hasNext();
    }

    @Test
    public void testNext() {
        ReverseIterator<Object, Object> reverseIterator = (ReverseIterator<Object, Object>) mock(ReverseIterator.class);
        when(reverseIterator.next()).thenReturn(new Pair<>());
        reverseIterator.next();
        verify(reverseIterator).next();
    }
}

