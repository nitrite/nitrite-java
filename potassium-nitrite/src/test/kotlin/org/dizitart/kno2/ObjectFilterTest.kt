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

package org.dizitart.kno2

import org.dizitart.kno2.filters.*
import org.dizitart.no2.collection.IndexType
import org.dizitart.no2.filters.ObjectFilters
import org.dizitart.no2.index.annotations.Id
import org.dizitart.no2.index.annotations.Index
import org.dizitart.no2.index.annotations.Indices
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 *
 * @author Anindya Chatterjee
 */
class ObjectFilterTest : BaseTest() {

    @Before
    fun before() {
        db = nitrite {
            path = fileName
        }
    }

    @Test
    fun testEq() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id eq 1)
            Assert.assertEquals(cursor.size(), 1)

            cursor = find(TestData::id eq 3)
            Assert.assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testGt() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id gt 1)
            Assert.assertEquals(cursor.size(), 1)

            cursor = find(TestData::id gt 2)
            Assert.assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testGte() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id gte 1)
            Assert.assertEquals(cursor.size(), 2)

            cursor = find(TestData::id gte 2)
            Assert.assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testLt() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id lt 1)
            Assert.assertEquals(cursor.size(), 0)

            cursor = find(TestData::id lt 2)
            Assert.assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testLte() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id lte 2)
            Assert.assertEquals(cursor.size(), 2)

            cursor = find(TestData::id lte 1)
            Assert.assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testWithin() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id within 1..2)
            Assert.assertEquals(cursor.size(), 2)

            cursor = find(TestData::id within arrayOf(2, 3))
            Assert.assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testElemMatch() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red", list = listOf(ListData("a", 1), ListData("b", 2))),
                    TestData(2, "yellow", list = listOf(ListData("a", 9), ListData("b", 10))))

            var cursor = find(TestData::list elemMatch (ListData::score eq 4))
            Assert.assertEquals(cursor.size(), 0)

            cursor = find(TestData::list elemMatch (ListData::name eq "b"))
            Assert.assertEquals(cursor.size(), 2)
        }
    }

    @Test
    fun testText() {
        db?.getRepository<TestData> {
            insert(TestData(1, "lorem ipsum dolor"), TestData(2, "quick brown fox"))

            var cursor = find(TestData::text text "*u*")
            Assert.assertEquals(cursor.size(), 2)

            cursor = find(TestData::text text "u*")
            Assert.assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testRegex() {
        db?.getRepository<TestData> {
            insert(TestData(1, "lorem"), TestData(2, "12345"))

            var cursor = find(TestData::text regex "[a-z]+")
            Assert.assertEquals(cursor.size(), 1)

            cursor = find(TestData::text regex "[0-9]+")
            Assert.assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testAnd() {
        db?.getRepository<TestData> {
            insert(TestData(1, "lorem"), TestData(2, "12345"))

            var cursor = find((TestData::id eq 1) and (TestData::text text "lorem"))
            Assert.assertEquals(cursor.size(), 1)

            cursor = find((TestData::id eq 1) and (TestData::text text "12345"))
            Assert.assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testOr() {
        db?.getRepository<TestData> {
            insert(TestData(1, "lorem"), TestData(2, "12345"))

            var cursor = find((TestData::id eq 1) or (TestData::text text "12345"))
            Assert.assertEquals(cursor.size(), 2)

            cursor = find((TestData::id eq 3) or (TestData::text text "123456"))
            Assert.assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testNot() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(!(TestData::id lt 1))
            Assert.assertEquals(cursor.size(), 2)

            cursor = find(!(TestData::id lt 2))
            Assert.assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testIssue58() {
        val repository = db?.getRepository(SimpleObject::class.java)!!
        val executor = Executors.newFixedThreadPool(10)

        val uuid = UUID.randomUUID()
        val latch = CountDownLatch(100)

        repository.update(SimpleObject(
                uuid,
                true
        ), true)

        for(i in 0..100) {
            executor.submit {
                val simpleObject = try {
                    repository.find(
                            ObjectFilters.eq(SimpleObject::id.name, uuid)
                    ).first()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    latch.countDown()
                    return@submit
                }

                repository.update(simpleObject.copy(
                        value = !simpleObject.value
                ))

                executor.submit {
                    try {
                        val result = repository.find(
                                ObjectFilters.eq(SimpleObject::id.name, uuid)
                        )
                        assertEquals(result.totalCount(), 1)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    } finally {
                        latch.countDown()
                    }
                }
            }
        }
        latch.await()
    }
}

@Indices(Index(value = "text", type = IndexType.Fulltext))
data class TestData(@Id val id: Int, val text: String, val list: List<ListData> = listOf())

class ListData(val name: String, val score: Int)

data class SimpleObject(
        @Id val id: UUID,
        val value: Boolean
)

