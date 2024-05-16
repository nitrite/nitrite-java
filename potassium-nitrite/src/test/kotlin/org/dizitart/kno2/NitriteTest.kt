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

package org.dizitart.kno2

import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.filters.text
import org.dizitart.no2.collection.FindOptions.orderBy
import org.dizitart.no2.collection.FindOptions.skipBy
import org.dizitart.no2.common.SortOrder
import org.dizitart.no2.exceptions.UniqueConstraintException
import org.dizitart.no2.index.IndexOptions.indexOptions
import org.dizitart.no2.index.IndexType
import org.dizitart.no2.mvstore.MVStoreModule
import org.dizitart.no2.repository.EntityDecorator
import org.dizitart.no2.repository.EntityId
import org.dizitart.no2.repository.EntityIndex
import org.dizitart.no2.repository.annotations.Id
import org.dizitart.no2.repository.annotations.Index
import org.dizitart.no2.repository.annotations.InheritIndices
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 *
 * @author Anindya Chatterjee
 */
class NitriteTest : BaseTest() {
    @Before
    fun before() {
        db = nitrite {
            loadModule(MVStoreModule(fileName))
            loadModule(KNO2Module())
        }
    }

    @Test
    fun testGetCollection() {
        db?.getCollection("test") {
            assertTrue(isOpen)
            close()
            assertFalse(isOpen)
        }
    }

    @Test
    fun testGetRepository() {
        db?.getRepository<TestData> {
            assertTrue(isOpen)
            close()
            assertFalse(isOpen)
        }
    }

    @Test
    fun testGetRepositoryWithKey() {
        db?.getRepository<TestData>("tag") {
            assertTrue(isOpen)
            close()
            assertFalse(isOpen)
        }
    }

    @Test
    fun testGetRepositoryWithEntityDecorator() {
        db?.getRepository(TestDataClassDecorator()) {
            assertTrue(isOpen)
            close()
            assertFalse(isOpen)
        }
    }

    @Test
    fun testGetRepositoryWithEntityDecoratorAndKey() {
        db?.getRepository(TestDataClassDecorator(), "tag") {
            assertTrue(isOpen)
            close()
            assertFalse(isOpen)
        }
    }

    @Test
    fun testIndexOption() {
        db?.getCollection("test") {
            createIndex(option(IndexType.UNIQUE), "id")
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
            var cursor = find(skipBy(0).limit(2))
            assertEquals(cursor.size(), 2)

            cursor = find(orderBy("a", SortOrder.Descending))
            assertEquals(cursor.size(), 5)
            assertEquals(cursor.last()["a"], 1)

            cursor = find(orderBy("a", SortOrder.Descending).skip(0).limit(2))
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

    @Test
    fun testIssue55() {
        val repository = db?.getRepository<CaObject>()!!
        val uuid = UUID.randomUUID()
        val executor = Executors.newFixedThreadPool(10)
        val latch = CountDownLatch(200)
        for (i in 0..100) {
            val item = CaObject(uuid, "1234")
            executor.submit {
                try {
                    repository.update(item, true)
                } catch (e: UniqueConstraintException) {
                    fail("Synchronization failed on update")
                } finally {
                    latch.countDown()
                }
            }
            executor.submit {
                try {
                    repository.insert(item)
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await()
        val cursor = repository.find()
        assertEquals(cursor.size(), 1)
    }

    @Test
    fun testIssue59() {
        val repository = db?.getRepository<ClassWithLocalDateTime>()!!
        repository.insert(ClassWithLocalDateTime("test", LocalDateTime.now()))
        assertNotNull(repository.find())
    }

    @Test
    fun testIssue222() {
        val first = NestedObjects("value1", "1", listOf(TempObject("name-1", 42, LevelUnder("street", 12))))
        val repository = db?.getRepository<NestedObjects>()!!
        repository.insert(first)

        repository.createIndex(indexOptions(IndexType.FULL_TEXT), "ob1");
        var found = repository.find("ob1" text "value1")
        assertFalse(found.isEmpty)

        first.ob1 = "value2"
        repository.update(first)

        found = repository.find("ob1" text "value2")
        assertFalse(found.isEmpty)

        found = repository.find("ob1" text "value1")
        assertTrue(found.isEmpty)
    }

    @Test
    fun testInMemory() {
        val db2 = nitrite()
        val collection = db2.getCollection("test");
        collection.insert(documentOf("a" to 1))

        val cursor = collection.find("a" eq 1)
        assertEquals(cursor.size(), 1)
    }
}

interface MyInterface {
    val id: UUID
}

@Index(fields = ["name"], type = IndexType.NON_UNIQUE)
abstract class SomeAbsClass(
        @Id override val id: UUID = UUID.randomUUID(),
        open val name: String = "abcd"
) : MyInterface {
    abstract val checked: Boolean
}

@InheritIndices
class MyClass(
    override val id: UUID = UUID.randomUUID(),
    override val name: String = "",
    override val checked: Boolean = false) : SomeAbsClass(id, name)

@InheritIndices
class MyClass2(
    override val id: UUID = UUID.randomUUID(),
    override val name: String = "",
    override val checked: Boolean = false,
    val importance: Int = 0
) : SomeAbsClass(id, name)

data class CaObject(
        @Id val localId: UUID = UUID.randomUUID(),
        val name: String = ""
)

@Index(fields = ["time"], type = IndexType.UNIQUE)
data class ClassWithLocalDateTime(
    val name: String = "",
    val time: LocalDateTime = LocalDateTime.now()
)

data class NestedObjects(var ob1: String = "", @Id val id: String = "", val list: List<TempObject> = listOf())
data class TempObject(val name: String = "", val aga: Int = 0, val add: LevelUnder = LevelUnder())
data class LevelUnder(val street: String = "", val number: Int = 0)

data class TestDataClass(
    val id: String = "",
    val name: String = "",
    val age: Int = 0
)

class TestDataClassDecorator : EntityDecorator<TestDataClass> {
    override fun getEntityType(): Class<TestDataClass> {
        return TestDataClass::class.java
    }

    override fun getIdField(): EntityId {
        return EntityId("id")
    }

    override fun getIndexFields(): MutableList<EntityIndex> {
        return mutableListOf(EntityIndex(IndexType.NON_UNIQUE, "name"))
    }
}

