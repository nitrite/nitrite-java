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

package org.dizitart.no2.mapper.jackson.integration.repository;

import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.mapper.jackson.integration.repository.data.SubEmployee;
import org.junit.Test;

import java.util.Iterator;

import static org.dizitart.no2.collection.FindOptions.skipBy;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class ProjectionTest extends BaseObjectRepositoryTest {

    @Test
    public void testHasMore() {
        RecordStream<SubEmployee> iterable = employeeRepository.find(skipBy(0).limit(5))
            .project(SubEmployee.class);
        assertFalse(iterable.isEmpty());
    }

    @Test
    public void testSize() {
        RecordStream<SubEmployee> iterable = employeeRepository.find(skipBy(0).limit(5))
            .project(SubEmployee.class);
        assertEquals(iterable.size(), 5);
    }

    @Test
    public void testToString() {
        RecordStream<SubEmployee> iterable = employeeRepository.find(skipBy(0).limit(5))
            .project(SubEmployee.class);
        assertNotNull(iterable.toString());
    }

    @Test(expected = InvalidOperationException.class)
    public void testRemove() {
        RecordStream<SubEmployee> iterable = employeeRepository.find(skipBy(0).limit(5))
            .project(SubEmployee.class);
        Iterator<SubEmployee> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
