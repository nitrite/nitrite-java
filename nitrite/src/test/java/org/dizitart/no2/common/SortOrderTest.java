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

package org.dizitart.no2.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SortOrderTest {

    @Test
    public void testValueOf() {
        assertEquals(SortOrder.Ascending, SortOrder.valueOf("Ascending"));
    }

    @Test
    public void testValues() {
        SortOrder[] actualValuesResult = SortOrder.values();
        assertEquals(2, actualValuesResult.length);
        assertEquals(SortOrder.Ascending, actualValuesResult[0]);
        assertEquals(SortOrder.Descending, actualValuesResult[1]);
    }
}

