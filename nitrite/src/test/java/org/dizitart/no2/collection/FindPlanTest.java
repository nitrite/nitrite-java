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

import org.dizitart.no2.common.Fields;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.IndexScanFilter;
import org.dizitart.no2.index.IndexDescriptor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class FindPlanTest {
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

    @Test
    public void testEquals11() {
        FindPlan findPlan = new FindPlan();

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setSkip(0L);
        assertFalse(findPlan.equals(findPlan1));
    }

    @Test
    public void testEquals12() {
        FindPlan findPlan = new FindPlan();

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setBlockingSortOrder(null);
        assertFalse(findPlan.equals(findPlan1));
    }

    @Test
    public void testEquals13() {
        FindPlan findPlan = new FindPlan();
        findPlan.setSubPlans(null);

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setSubPlans(null);
        assertTrue(findPlan.equals(findPlan1));
    }

    @Test
    public void testEquals14() {
        FindPlan findPlan = new FindPlan();
        findPlan.setLimit(0L);

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setLimit(0L);
        assertTrue(findPlan.equals(findPlan1));
    }

    @Test
    public void testEquals15() {
        FindPlan findPlan = new FindPlan();
        findPlan.setSkip(0L);

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setSkip(0L);
        assertTrue(findPlan.equals(findPlan1));
    }

    @Test
    public void testEquals16() {
        FindPlan findPlan = new FindPlan();
        findPlan.setBlockingSortOrder(null);

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setBlockingSortOrder(null);
        assertTrue(findPlan.equals(findPlan1));
    }

    @Test
    public void testEquals17() {
        FindPlan findPlan = new FindPlan();
        findPlan.setIndexDescriptor(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        assertFalse(findPlan.equals(new FindPlan()));
    }

    @Test
    public void testEquals18() {
        FindPlan findPlan = new FindPlan();
        findPlan.setIndexScanFilter(new IndexScanFilter(new ArrayList<Filter>()));
        assertFalse(findPlan.equals(new FindPlan()));
    }

    @Test
    public void testEquals19() {
        FindPlan findPlan = new FindPlan();
        findPlan.setIndexScanOrder(new HashMap<String, Boolean>(1));
        assertFalse(findPlan.equals(new FindPlan()));
    }

    @Test
    public void testEquals2() {
        FindPlan findPlan = new FindPlan();
        assertTrue(findPlan.equals(new FindPlan()));
    }

    @Test
    public void testEquals20() {
        FindPlan findPlan = new FindPlan();

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setIndexDescriptor(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        assertFalse(findPlan.equals(findPlan1));
    }

    @Test
    public void testEquals21() {
        FindPlan findPlan = new FindPlan();

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setIndexScanFilter(new IndexScanFilter(new ArrayList<Filter>()));
        assertFalse(findPlan.equals(findPlan1));
    }

    @Test
    public void testEquals3() {
        FindPlan findPlan = new FindPlan();
        findPlan.setSubPlans(null);
        assertFalse(findPlan.equals(new FindPlan()));
    }

    @Test
    public void testEquals4() {
        FindPlan findPlan = new FindPlan();
        findPlan.setLimit(0L);
        assertFalse(findPlan.equals(new FindPlan()));
    }

    @Test
    public void testEquals5() {
        FindPlan findPlan = new FindPlan();
        findPlan.setCollectionScanFilter(mock(Filter.class));
        assertFalse(findPlan.equals(new FindPlan()));
    }

    @Test
    public void testEquals6() {
        FindPlan findPlan = new FindPlan();
        findPlan.setSkip(0L);
        assertFalse(findPlan.equals(new FindPlan()));
    }

    @Test
    public void testEquals7() {
        FindPlan findPlan = new FindPlan();
        findPlan.setBlockingSortOrder(null);
        assertFalse(findPlan.equals(new FindPlan()));
    }

    @Test
    public void testEquals8() {
        FindPlan findPlan = new FindPlan();

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setSubPlans(null);
        assertFalse(findPlan.equals(findPlan1));
    }

    @Test
    public void testEquals9() {
        FindPlan findPlan = new FindPlan();

        FindPlan findPlan1 = new FindPlan();
        findPlan1.setLimit(0L);
        assertFalse(findPlan.equals(findPlan1));
    }

    @Test
    public void testHashCode() {
        assertEquals(157971442, (new FindPlan()).hashCode());
    }

    @Test
    public void testHashCode10() {
        ArrayList<FindPlan> findPlanList = new ArrayList<FindPlan>();
        findPlanList.add(new FindPlan());

        FindPlan findPlan = new FindPlan();
        findPlan.setSubPlans(findPlanList);
        assertEquals(315942914, findPlan.hashCode());
    }

    @Test
    public void testHashCode11() {
        ArrayList<FindPlan> findPlanList = new ArrayList<FindPlan>();
        findPlanList.add(new FindPlan());
        findPlanList.add(new FindPlan());

        FindPlan findPlan = new FindPlan();
        findPlan.setSubPlans(findPlanList);
        assertEquals(918091250, findPlan.hashCode());
    }

    @Test
    public void testHashCode2() {
        FindPlan findPlan = new FindPlan();
        findPlan.setSubPlans(null);
        assertEquals(157971484, findPlan.hashCode());
    }

    @Test
    public void testHashCode3() {
        FindPlan findPlan = new FindPlan();
        findPlan.setLimit(0L);
        assertEquals(1549271361, findPlan.hashCode());
    }

    @Test
    public void testHashCode4() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        FindPlan findPlan = new FindPlan();
        findPlan.setCollectionScanFilter(mock(Filter.class));
        findPlan.hashCode();
    }

    @Test
    public void testHashCode5() {
        FindPlan findPlan = new FindPlan();
        findPlan.setSkip(0L);
        assertEquals(640288039, findPlan.hashCode());
    }

    @Test
    public void testHashCode6() {
        FindPlan findPlan = new FindPlan();
        findPlan.setBlockingSortOrder(null);
        assertEquals(158117644, findPlan.hashCode());
    }

    @Test
    public void testHashCode7() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        FindPlan findPlan = new FindPlan();
        findPlan.setIndexDescriptor(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        findPlan.hashCode();
    }

    @Test
    public void testHashCode8() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        FindPlan findPlan = new FindPlan();
        findPlan.setIndexScanFilter(new IndexScanFilter(new ArrayList<Filter>()));
        findPlan.hashCode();
    }

    @Test
    public void testHashCode9() {
        FindPlan findPlan = new FindPlan();
        findPlan.setIndexScanOrder(new HashMap<String, Boolean>(1));
        assertEquals(-363075081, findPlan.hashCode());
    }
}

