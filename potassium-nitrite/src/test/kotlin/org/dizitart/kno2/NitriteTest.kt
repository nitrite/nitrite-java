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
import org.dizitart.no2.objects.Id
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
        val repository = db?.getRepository<SomeAbsClass>()!!
        assertTrue(repository.hasIndex("id"))
        assertTrue(repository.hasIndex("name"))

        val item = MyClass(UUID.randomUUID(), "xyz", true)
        var writeResult = repository.insert(item)
        assertEquals(writeResult.affectedCount, 1)

        var cursor = repository.find()
        assertEquals(cursor.size(), 1)

        val item2 = MyClass2(UUID.randomUUID(), "123", true, 3)
        writeResult = repository.insert(item2)
        assertEquals(writeResult.affectedCount, 1)

        cursor = repository.find()
        assertEquals(cursor.size(), 2)
    }
}

interface MyInterface {
    val id: UUID
}

@Indices(value = [(Index(value = "name", type = IndexType.NonUnique))])
abstract class SomeAbsClass (
        @Id override val id: UUID = UUID.randomUUID(),
        open val name: String = "abcd"
) : MyInterface {
    abstract val checked: Boolean
}

@InheritIndices
class MyClass(
        override val id: UUID,
        override val name: String,
        override val checked: Boolean) : SomeAbsClass(id, name)

@InheritIndices
class MyClass2(
        override val id: UUID,
        override val name: String,
        override val checked: Boolean,
        val importance: Int
) : SomeAbsClass(id, name)