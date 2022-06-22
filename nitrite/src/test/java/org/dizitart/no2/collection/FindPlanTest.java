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

package org.dizitart.no2.collection;

import org.dizitart.no2.filters.Filter;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class FindPlanTest {
    @Test
    public void testConstructor() {
        FindPlan actualFindPlan = new FindPlan();
        assertTrue(actualFindPlan.getBlockingSortOrder().isEmpty());
        assertEquals(
            "FindPlan(byIdFilter=null, indexScanFilter=null, collectionScanFilter=null, indexDescriptor=null,"
                + " indexScanOrder=null, blockingSortOrder=[], skip=null, limit=null, distinct=false, " +
                "collator=null, subPlans=[])",
            actualFindPlan.toString());
        assertTrue(actualFindPlan.getSubPlans().isEmpty());
        assertNull(actualFindPlan.getSkip());
        assertNull(actualFindPlan.getLimit());
        assertNull(actualFindPlan.getIndexScanOrder());
        assertNull(actualFindPlan.getIndexScanFilter());
        assertNull(actualFindPlan.getIndexDescriptor());
        assertNull(actualFindPlan.getCollectionScanFilter());
        assertNull(actualFindPlan.getCollator());
        assertNull(actualFindPlan.getByIdFilter());
    }

    @Test
    public void testCanEqual() {
        assertFalse((new FindPlan()).canEqual("Other"));
    }

    @Test
    public void testCanEqual2() {
        FindPlan findPlan = new FindPlan();
        assertTrue(findPlan.canEqual(new FindPlan()));
    }

    @Test
    public void testEquals() {
        assertFalse((new FindPlan()).equals("42"));
    }

    @Test
    public void testEquals10() {
        FindPlan findPlan = new FindPlan();

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setCollectionScanFilter(mock(Filter.class));
        assertFalse(findPlan.equals(findPlan1));
    }
}

