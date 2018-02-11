/*
 *
 * Copyright 2017 Nitrite author or authors.
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

package org.dizitart.kno2

import org.dizitart.no2.IndexType
import org.dizitart.no2.SortOrder
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices
import org.dizitart.no2.objects.InheritIndices
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 *
 * @author Anindya Chatterjee
 */
class NitriteTest : BaseTest() {
    @Before
    fun before() {
        db = nitrite {
            path = fileName
        }
    }

    @Test
    fun testGetCollection() {
        db?.getCollection("test") {
            assertFalse(isClosed)
            close()
            assertTrue(isClosed)
        }
    }

    @Test
    fun testGetRepository() {
        db?.getRepository<TestData> {
            assertFalse(isClosed)
            close()
            assertTrue(isClosed)
        }
    }

    @Test
    fun testIndexOption() {
        db?.getCollection("test") {
            createIndex("id", option(IndexType.Unique, true))
            assertTrue(hasIndex("id"))
            assertFalse(hasIndex("name"))
        }
    }

    @Test
    fun testFindOption() {
        db?.getCollection("test") {
            insert(documentOf("a" to 1),
                    documentOf("a" to 2),
                    documentOf("a" to 3),
                    documentOf("a" to 4),
                    documentOf("a" to 5))
            var cursor = find(limit(0, 2))
            assertEquals(cursor.size(), 2)

            cursor = find(sort("a", SortOrder.Descending))
            assertEquals(cursor.size(), 5)
            assertEquals(cursor.last()["a"], 1)

            cursor = find(sort("a", SortOrder.Descending).thenLimit(0, 2))
            assertEquals(cursor.size(), 2)
            assertEquals(cursor.last()["a"], 4)
        }
    }

    @Test
    fun testIssue54() {
        val repository = db?.getRepository<TestDataClass>()!!
        assertTrue(repository.hasIndex("dateTime"))
        assertTrue(repository.hasIndex("checked"))
    }
}

@Indices(value = [(Index(value = "dateTime", type = IndexType.NonUnique)),
    (Index(value = "checked", type = IndexType.NonUnique))])
interface MyInterface {
    val localId: UUID
    val checked: Boolean
    val dateTime: String
    val importance: Int
}

@InheritIndices
data class TestDataClass(
        override val localId: UUID,
        override val checked: Boolean,
        override val dateTime: String,
        override val importance: Int,
        val anotherField: String
) : MyInterface