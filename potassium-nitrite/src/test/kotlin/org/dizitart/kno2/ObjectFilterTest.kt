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

import org.dizitart.kno2.filters.*
import org.dizitart.no2.index.IndexType
import org.dizitart.no2.repository.annotations.Id
import org.dizitart.no2.repository.annotations.Index
import org.dizitart.no2.repository.annotations.Indices
import org.dizitart.no2.spatial.SpatialIndexer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.WKTReader
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
            assertEquals(cursor.size(), 1)

            cursor = find(TestData::id eq 3)
            assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testGt() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id gt 1)
            assertEquals(cursor.size(), 1)

            cursor = find(TestData::id gt 2)
            assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testGte() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id gte 1)
            assertEquals(cursor.size(), 2)

            cursor = find(TestData::id gte 2)
            assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testLt() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id lt 1)
            assertEquals(cursor.size(), 0)

            cursor = find(TestData::id lt 2)
            assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testLte() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id lte 2)
            assertEquals(cursor.size(), 2)

            cursor = find(TestData::id lte 1)
            assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testWithin() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(TestData::id within 1..2)
            assertEquals(cursor.size(), 2)

            cursor = find(TestData::id within arrayOf(2, 3))
            assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testElemMatch() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red", list = listOf(ListData("a", 1), ListData("b", 2))),
                TestData(2, "yellow", list = listOf(ListData("a", 9), ListData("b", 10))))

            var cursor = find(TestData::list elemMatch (ListData::score eq 4))
            assertEquals(cursor.size(), 0)

            cursor = find(TestData::list elemMatch (ListData::name eq "b"))
            assertEquals(cursor.size(), 2)
        }
    }

    @Test
    fun testText() {
        db?.getRepository<TestData> {
            insert(TestData(1, "lorem ipsum dolor"), TestData(2, "quick brown fox"))

            var cursor = find(TestData::text text "*u*")
            assertEquals(cursor.size(), 2)

            cursor = find(TestData::text text "u*")
            assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testRegex() {
        db?.getRepository<TestData> {
            insert(TestData(1, "lorem"), TestData(2, "12345"))

            var cursor = find(TestData::text regex "[a-z]+")
            assertEquals(cursor.size(), 1)

            cursor = find(TestData::text regex "[0-9]+")
            assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testAnd() {
        db?.getRepository<TestData> {
            insert(TestData(1, "lorem"), TestData(2, "12345"))

            var cursor = find((TestData::id eq 1) and (TestData::text text "lorem"))
            assertEquals(cursor.size(), 1)

            cursor = find((TestData::id eq 1) and (TestData::text text "12345"))
            assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testOr() {
        db?.getRepository<TestData> {
            insert(TestData(1, "lorem"), TestData(2, "12345"))

            var cursor = find((TestData::id eq 1) or (TestData::text text "12345"))
            assertEquals(cursor.size(), 2)

            cursor = find((TestData::id eq 3) or (TestData::text text "123456"))
            assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testNot() {
        db?.getRepository<TestData> {
            insert(TestData(1, "red"), TestData(2, "yellow"))

            var cursor = find(!(TestData::id lt 1))
            assertEquals(cursor.size(), 2)

            cursor = find(!(TestData::id lt 2))
            assertEquals(cursor.size(), 1)
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

        for (i in 0..100) {
            executor.submit {
                val simpleObject = try {
                    repository.find(SimpleObject::id.name eq uuid).first()
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
                        val result = repository
                            .find(SimpleObject::id.name eq uuid)
                        assertEquals(result.size(), 1)
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

    @Test
    fun testIntersects() {
        val reader = WKTReader()
        val search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))")

        db?.getRepository<SpatialData> {
            val object1 = SpatialData(1L, reader.read("POINT(500 505)"))
            val object2 = SpatialData(2L, reader.read("LINESTRING(550 551, 525 512, 565 566)"))
            val object3 = SpatialData(3L, reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))"))

            insert(object1, object2, object3)

            val cursor = find(SpatialData::geometry intersects search)
            assertEquals(cursor.size(), 2)
            assertEquals(cursor.toList(), listOf(object1, object2))
        }
    }

    @Test
    fun testGeoWithin() {
        val reader = WKTReader()
        val search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))")

        db?.getRepository<SpatialData> {
            val object1 = SpatialData(1L, reader.read("POINT(500 505)"))
            val object2 = SpatialData(2L, reader.read("LINESTRING(550 551, 525 512, 565 566)"))
            val object3 = SpatialData(3L, reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))"))

            insert(object1, object2, object3)

            val cursor = find(SpatialData::geometry within search)
            assertEquals(cursor.size().toLong(), 1)
            assertEquals(cursor.toList(), listOf(object1))
        }
    }

    @Test
    fun testNearPoint() {
        val reader = WKTReader()
        val search = reader.read("POINT (490 490)") as Point

        db?.getRepository<SpatialData> {
            val object1 = SpatialData(1L, reader.read("POINT(500 505)"))
            val object2 = SpatialData(2L, reader.read("LINESTRING(550 551, 525 512, 565 566)"))
            val object3 = SpatialData(3L, reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))"))

            insert(object1, object2, object3)

            val cursor = find(SpatialData::geometry.near(search, 20.0))
            assertEquals(cursor.size().toLong(), 1)
            assertEquals(cursor.toList(), listOf(object1))
        }
    }


    @Test
    fun testNearCoordinate() {
        val reader = WKTReader()
        val search = reader.read("POINT (490 490)") as Point
        val coordinate = search.coordinate

        db?.getRepository<SpatialData> {
            val object1 = SpatialData(1L, reader.read("POINT(500 505)"))
            val object2 = SpatialData(2L, reader.read("LINESTRING(550 551, 525 512, 565 566)"))
            val object3 = SpatialData(3L, reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))"))

            insert(object1, object2, object3)

            val cursor = find(SpatialData::geometry.near(coordinate, 20.0))
            assertEquals(cursor.size(), 1)
            assertEquals(cursor.toList(), listOf(object1))
        }
    }
}

@Indices(Index(value = "text", type = IndexType.Fulltext))
data class TestData(@Id val id: Int, val text: String, val list: List<ListData> = listOf())

class ListData(val name: String, val score: Int)

data class SimpleObject(
        @Id val id: UUID,
        val value: Boolean
)

@Index(value = "geometry", type = SpatialIndexer.SpatialIndex)
data class SpatialData(
        @Id val id: Long,
        val geometry: Geometry
)

