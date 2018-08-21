/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.objects;

import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.collection.SortOrder;
import org.dizitart.no2.collection.objects.data.ClassA;
import org.dizitart.no2.collection.objects.data.ClassC;
import org.junit.Test;

import static org.dizitart.no2.collection.FindOptions.sort;
import static org.dizitart.no2.filters.ObjectFilters.*;
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
        assertFalse(cursor.hasMore());

        IndexOptions indexOptions = new IndexOptions();
        indexOptions.setIndexType(IndexType.Unique);
        aObjectRepository.createIndex("b.number", indexOptions);

        cursor = aObjectRepository.find(not(eq("b.number", 160)),
                sort("b.number", SortOrder.Ascending).thenLimit(0, 10));

        System.out.println("Available - " + cursor.hasMore());
        System.out.println("Total Size - " + cursor.totalCount());

        Iterable<ClassA> findRecord = cursor.project(ClassA.class);
        for(ClassA classA : findRecord) {
            System.out.println(classA);
        }

        cursor = aObjectRepository.find(not(eq("b.number", 160)),
                sort("b.number", SortOrder.Descending).thenLimit(2, 7));

        System.out.println("Available - " + cursor.hasMore());
        System.out.println("Total Size - " + cursor.totalCount());

        findRecord = cursor.project(ClassA.class);
        for(ClassA classA : findRecord) {
            System.out.println(classA);
        }

        cursor = cObjectRepository.find(gt("id", 900),
                sort("id", SortOrder.Descending).thenLimit(2, 7));
        System.out.println("Available - " + cursor.hasMore());
        System.out.println("Total Size - " + cursor.totalCount());

        Iterable<ClassC> findRecordC = cursor.project(ClassC.class);
        for(ClassC classC : findRecordC) {
            System.out.println(classC);
        }
    }
}
