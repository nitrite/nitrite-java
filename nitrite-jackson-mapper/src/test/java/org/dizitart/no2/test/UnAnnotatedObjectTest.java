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

import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.test.data.ClassA;
import org.dizitart.no2.test.data.ClassC;
import org.junit.Test;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Anindya Chatterjee.
 */
public class UnAnnotatedObjectTest extends BaseObjectRepositoryTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testFind() {
        Cursor cursor = aObjectRepository.find();
        assertEquals(cursor.size(), 10);
        assertFalse(cursor.isEmpty());

        IndexOptions indexOptions = new IndexOptions();
        indexOptions.setIndexType(IndexType.Unique);
        aObjectRepository.createIndex("b.number", indexOptions);

        cursor = aObjectRepository.find(where("b.number").eq(160).not()).
            sort("b.number", SortOrder.Ascending).limit(0, 10);

        System.out.println("Available - " + !cursor.isEmpty());
        System.out.println("Total Size - " + cursor.size());

        Iterable<ClassA> findRecord = cursor.project(ClassA.class);
        for (ClassA classA : findRecord) {
            System.out.println(classA);
        }

        cursor = aObjectRepository.find(where("b.number").eq(160).not()).
            sort("b.number", SortOrder.Descending).limit(2, 7);

        System.out.println("Available - " + !cursor.isEmpty());
        System.out.println("Total Size - " + cursor.size());

        findRecord = cursor.project(ClassA.class);
        for (ClassA classA : findRecord) {
            System.out.println(classA);
        }

        cursor = cObjectRepository.find(where("id").gt(900)).
            sort("id", SortOrder.Descending).limit(2, 7);
        System.out.println("Available - " + !cursor.isEmpty());
        System.out.println("Total Size - " + cursor.size());

        Iterable<ClassC> findRecordC = cursor.project(ClassC.class);
        for (ClassC classC : findRecordC) {
            System.out.println(classC);
        }
    }
}
