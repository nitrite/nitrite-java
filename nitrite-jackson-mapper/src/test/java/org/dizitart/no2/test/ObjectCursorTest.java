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

import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.test.data.Employee;
import org.junit.Test;

import java.util.AbstractCollection;

/**
 * @author Anindya Chatterjee
 */
public class ObjectCursorTest extends BaseObjectRepositoryTest {

    @Test(expected = ValidationException.class)
    public void testProjectForInterface() {
        Cursor<Employee> cursor = employeeRepository.find();
        cursor.project(Comparable.class);
    }

    @Test(expected = ValidationException.class)
    public void testProjectForPrimitive() {
        Cursor<Employee> cursor = employeeRepository.find();
        cursor.project(int.class);
    }

    @Test(expected = ValidationException.class)
    public void testProjectForArray() {
        Cursor<Employee> cursor = employeeRepository.find();
        cursor.project(String[].class);
    }

    @Test(expected = ValidationException.class)
    public void testProjectForAbstractClass() {
        Cursor<Employee> cursor = employeeRepository.find();
        cursor.project(AbstractCollection.class);
    }
}
