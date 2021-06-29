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

public class OrFilterTest {
    @Test
    public void testConstructor() {
        assertFalse((new OrFilter(mock(Filter.class), mock(Filter.class), mock(Filter.class))).getObjectFilter());
    }

    @Test
    public void testApply() {
        Filter filter = mock(Filter.class);
        when(filter.apply(any())).thenReturn(true);
        OrFilter orFilter = new OrFilter(filter, mock(Filter.class), mock(Filter.class));
        assertTrue(orFilter.apply(new Pair<>()));
        verify(filter).apply(any());
    }

    @Test
    public void testApply2() {
        Filter filter = mock(Filter.class);
        when(filter.apply(any())).thenReturn(false);
        Filter filter1 = mock(Filter.class);
        when(filter1.apply(any())).thenReturn(true);
        OrFilter orFilter = new OrFilter(filter, filter1, mock(Filter.class));
        assertTrue(orFilter.apply(new Pair<>()));
        verify(filter1).apply(any());
        verify(filter).apply(any());
    }
}

