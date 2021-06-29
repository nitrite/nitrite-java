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

import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.junit.Assert.*;

public class BetweenFilterTest {
    @Test
    public void testBoundConstructor() {
        BetweenFilter.Bound<Object> actualBound = new BetweenFilter.Bound<>("Lower Bound", "Upper Bound");
        Object lowerBound = actualBound.getLowerBound();
        assertTrue(lowerBound instanceof String);
        assertEquals(
                "BetweenFilter.Bound(upperBound=Upper Bound, lowerBound=Lower Bound, upperInclusive=true, lowerInclusive"
                        + "=true)",
                actualBound.toString());
        assertEquals("Lower Bound", lowerBound);
        assertTrue(actualBound.isUpperInclusive());
        assertTrue(actualBound.isLowerInclusive());
        Object upperBound = actualBound.getUpperBound();
        assertTrue(upperBound instanceof String);
        assertEquals("Upper Bound", upperBound);
    }

    @Test
    public void testBoundConstructor2() {
        BetweenFilter.Bound<Object> actualBound = new BetweenFilter.Bound<>("Lower Bound", "Upper Bound", true);
        Object lowerBound = actualBound.getLowerBound();
        assertTrue(lowerBound instanceof String);
        assertEquals(
                "BetweenFilter.Bound(upperBound=Upper Bound, lowerBound=Lower Bound, upperInclusive=true, lowerInclusive"
                        + "=true)",
                actualBound.toString());
        assertEquals("Lower Bound", lowerBound);
        assertTrue(actualBound.isUpperInclusive());
        assertTrue(actualBound.isLowerInclusive());
        Object upperBound = actualBound.getUpperBound();
        assertTrue(upperBound instanceof String);
        assertEquals("Upper Bound", upperBound);
    }

    @Test
    public void testBoundConstructor3() {
        BetweenFilter.Bound<Object> actualBound = new BetweenFilter.Bound<>("Lower Bound", "Upper Bound", true, true);
        Object lowerBound = actualBound.getLowerBound();
        assertTrue(lowerBound instanceof String);
        assertEquals(
                "BetweenFilter.Bound(upperBound=Upper Bound, lowerBound=Lower Bound, upperInclusive=true, lowerInclusive"
                        + "=true)",
                actualBound.toString());
        assertEquals("Lower Bound", lowerBound);
        assertTrue(actualBound.isUpperInclusive());
        assertTrue(actualBound.isLowerInclusive());
        Object upperBound = actualBound.getUpperBound();
        assertTrue(upperBound instanceof String);
        assertEquals("Upper Bound", upperBound);
    }

    @Test
    public void testConstructor() {
        BetweenFilter<Object> actualBetweenFilter = new BetweenFilter<>("Field",
            new BetweenFilter.Bound<>("Lower Bound", "Upper Bound"));
        assertEquals("((Field <= Upper Bound) && (Field >= Lower Bound))", actualBetweenFilter.toString());
        assertFalse(actualBetweenFilter.getObjectFilter());
    }

    @Test
    public void testConstructor2() {
        assertThrows(ValidationException.class,
                () -> new BetweenFilter<>("Field", new BetweenFilter.Bound<>(null, "Upper Bound")));
    }

    @Test
    public void testConstructor3() {
        assertThrows(ValidationException.class,
                () -> new BetweenFilter<>("Field", new BetweenFilter.Bound<>("Lower Bound", null)));
    }

    @Test
    public void testConstructor4() {
        assertThrows(ValidationException.class, () -> new BetweenFilter<>("Field", null));
    }
}

