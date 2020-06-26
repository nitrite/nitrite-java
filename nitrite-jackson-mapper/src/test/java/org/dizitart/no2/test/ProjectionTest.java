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

package org.dizitart.no2.test;

import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.test.data.SubEmployee;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class ProjectionTest extends BaseObjectRepositoryTest {

    @Test
    public void testHasMore() {
        ReadableStream<SubEmployee> iterable = employeeRepository.find().limit(0, 5)
            .project(SubEmployee.class);
        assertFalse(iterable.isEmpty());
    }

    @Test
    public void testSize() {
        ReadableStream<SubEmployee> iterable = employeeRepository.find().limit(0, 5)
            .project(SubEmployee.class);
        assertEquals(iterable.size(), 5);
    }

    @Test
    public void testToString() {
        ReadableStream<SubEmployee> iterable = employeeRepository.find().limit(0, 5)
            .project(SubEmployee.class);
        assertNotNull(iterable.toString());
    }

    @Test(expected = InvalidOperationException.class)
    public void testRemove() {
        ReadableStream<SubEmployee> iterable = employeeRepository.find().limit(0, 5)
            .project(SubEmployee.class);
        Iterator<SubEmployee> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
