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

package org.dizitart.no2.filters;

import org.dizitart.no2.common.tuples.Pair;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class NotFilterTest {
    @Test
    public void testConstructor() {
        assertFalse((new NotFilter(mock(Filter.class))).getObjectFilter());
    }

    @Test
    public void testApply() {
        Filter filter = mock(Filter.class);
        when(filter.apply(any())).thenReturn(true);
        NotFilter notFilter = new NotFilter(filter);
        assertFalse(notFilter.apply(new Pair<>()));
        verify(filter).apply(any());
    }

    @Test
    public void testApply2() {
        Filter filter = mock(Filter.class);
        when(filter.apply(any())).thenReturn(false);
        NotFilter notFilter = new NotFilter(filter);
        assertTrue(notFilter.apply(new Pair<>()));
        verify(filter).apply(any());
    }
}

