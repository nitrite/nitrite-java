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

package org.dizitart.no2.integration.repository;


import org.dizitart.no2.integration.repository.data.ClassA;
import org.dizitart.no2.integration.repository.data.ClassC;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.repository.Cursor;
import org.junit.Test;

import static org.dizitart.no2.collection.FindOptions.orderBy;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class UnAnnotatedObjectTest extends BaseObjectRepositoryTest {

    @Test
    public void testFind() {
        Cursor<?> cursor = aObjectRepository.find();
        assertEquals(cursor.size(), 10);
        assertFalse(cursor.isEmpty());

        aObjectRepository.createIndex("b.number");

        cursor = aObjectRepository.find(where("b.number").eq(160).not(),
            orderBy("b.number", SortOrder.Ascending).skip(0).limit(10));

        Iterable<ClassA> findRecord = cursor.project(ClassA.class);
        for (ClassA classA : findRecord) {
            assertNotNull(classA);
        }

        cursor = aObjectRepository.find(where("b.number").eq(160).not(),
            orderBy("b.number", SortOrder.Descending).skip(2).limit(7));

        findRecord = cursor.project(ClassA.class);
        for (ClassA classA : findRecord) {
            assertNotNull(classA);
        }

        cursor = cObjectRepository.find(where("id").gt(900),
            orderBy("id", SortOrder.Descending).skip(2).limit(7));

        Iterable<ClassC> findRecordC = cursor.project(ClassC.class);
        for (ClassC classC : findRecordC) {
            assertNotNull(classC);
        }
    }
}
