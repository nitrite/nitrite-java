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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class FindOptimizerTest {
    @Test
    public void testOptimize() {
        FindOptimizer findOptimizer = new FindOptimizer();
        Filter filter = mock(Filter.class);
        FindOptions findOptions = new FindOptions();
        FindPlan actualOptimizeResult = findOptimizer.optimize(filter, findOptions, new ArrayList<>());
        assertNull(actualOptimizeResult.getSkip());
        assertNull(actualOptimizeResult.getLimit());
        assertTrue(actualOptimizeResult.getCollator() instanceof java.text.RuleBasedCollator);
    }

    @Test
    public void testOptimize2() {
        FindOptimizer findOptimizer = new FindOptimizer();
        Filter filter = mock(Filter.class);
        findOptimizer.optimize(filter, null, new ArrayList<>());
    }

    @Test
    public void testOptimize3() {
        FindOptimizer findOptimizer = new FindOptimizer();
        Filter filter = mock(Filter.class);
        FindOptions findOptions = FindOptions.orderBy("Field Name", SortOrder.Ascending);
        FindPlan actualOptimizeResult = findOptimizer.optimize(filter, findOptions, new ArrayList<>());
        assertTrue(actualOptimizeResult.getCollator() instanceof java.text.RuleBasedCollator);
        assertNull(actualOptimizeResult.getSkip());
        assertNull(actualOptimizeResult.getLimit());
    }

    @Test
    public void testOptimize4() {
        FindOptimizer findOptimizer = new FindOptimizer();
        Filter filter = mock(Filter.class);
        FindOptions findOptions = new FindOptions();

        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        indexDescriptorList.add(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        FindPlan actualOptimizeResult = findOptimizer.optimize(filter, findOptions, indexDescriptorList);
        assertNull(actualOptimizeResult.getSkip());
        assertNull(actualOptimizeResult.getLimit());
        assertTrue(actualOptimizeResult.getCollator() instanceof java.text.RuleBasedCollator);
    }

    @Test
    public void testOptimize5() {
        FindOptimizer findOptimizer = new FindOptimizer();
        Filter filter = mock(Filter.class);
        FindOptions findOptions = new FindOptions();

        Fields fields = new Fields();
        fields.addField("Field");
        IndexDescriptor e = new IndexDescriptor("Index Type", fields, "Collection Name");

        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        indexDescriptorList.add(e);
        FindPlan actualOptimizeResult = findOptimizer.optimize(filter, findOptions, indexDescriptorList);
        assertNull(actualOptimizeResult.getSkip());
        assertNull(actualOptimizeResult.getLimit());
        assertTrue(actualOptimizeResult.getCollator() instanceof java.text.RuleBasedCollator);
    }
}

