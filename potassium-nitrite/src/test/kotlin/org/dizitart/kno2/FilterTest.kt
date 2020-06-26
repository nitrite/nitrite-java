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

import lombok.Getter
import org.dizitart.kno2.filters.*
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.Constants
import org.dizitart.no2.index.IndexOptions
import org.dizitart.no2.index.IndexType
import org.dizitart.no2.spatial.SpatialIndexer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.WKTReader
import java.util.*

/**
 *
 * @author Anindya Chatterjee
 */
class FilterTest : BaseTest() {

    @Before
    fun before() {
        db = nitrite {
            path = fileName
        }
    }

    @Test
    fun testEq() {
        db?.getCollection("test") {
            insert(documentOf("a" to 1))

            var cursor = find("a" eq 1)
            assertEquals(cursor.size(), 1)

            cursor = find("a" eq 2)
            assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testGt() {
        db?.getCollection("test") {
            insert(documentOf("a" to 1), documentOf("a" to 5))

            var cursor = find("a" gt 1)
            assertEquals(cursor.size(), 1)

            cursor = find("a" gt 0)
            assertEquals(cursor.size(), 2)
        }
    }

    @Test
    fun testGte() {
        db?.getCollection("test") {
            insert(documentOf("a" to 1), documentOf("a" to 5))

            var cursor = find("a" gte 1)
            assertEquals(cursor.size(), 2)

            cursor = find("a" gte 10)
            assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testLt() {
        db?.getCollection("test") {
            insert(documentOf("a" to 1), documentOf("a" to 5))

            var cursor = find("a" lt 5)
            assertEquals(cursor.size(), 1)

            cursor = find("a" lt 10)
            assertEquals(cursor.size(), 2)
        }
    }

    @Test
    fun testLte() {
        db?.getCollection("test") {
            insert(documentOf("a" to 1), documentOf("a" to 5))

            var cursor = find("a" lte 5)
            assertEquals(cursor.size(), 2)

            cursor = find("a" lte 1)
            assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testWithin() {
        db?.getCollection("test") {
            insert(documentOf("a" to 1), documentOf("a" to 5))

            var cursor = find("a" within arrayOf(1, 2, 5))
            assertEquals(cursor.size(), 2)

            cursor = find("a" within 1..5)
            assertEquals(cursor.size(), 2)

            cursor = find("a" within listOf(1, 2, 3, 4, 5))
            assertEquals(cursor.size(), 2)
        }
    }

    @Test
    fun testElemMatch() {
        db?.getCollection("test") {
            insert(documentOf("a" to listOf(1, 2, 3, 4, 5)),
                documentOf("a" to listOf(3, 4, 5, 6, 7, 8)))

            var cursor = find("a" elemMatch ("$" within 3..5))
            assertEquals(cursor.size(), 2)

            cursor = find("a" elemMatch ("$" eq 7))
            assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testText() {
        db?.getCollection("test") {
            createIndex("a", option(IndexType.Fulltext))

            insert(documentOf("a" to "Lorem ipsum dolor"),
                documentOf("a" to "quick brown fox jumps over lazy dog"))

            var cursor = find("a" text "*ipsum")
            assertEquals(cursor.size(), 1)

            cursor = find("a" text "*um*")
            assertEquals(cursor.size(), 2)
        }
    }

    @Test
    fun testRegex() {
        db?.getCollection("test") {
            createIndex("a", option(IndexType.Fulltext))

            insert(documentOf("a" to "lorem"),
                documentOf("a" to "dog"))

            var cursor = find("a" regex "[a-z]+")
            assertEquals(cursor.size(), 2)

            cursor = find("a" regex "[1-9]+")
            assertEquals(cursor.size(), 0)
        }
    }

    @Test
    fun testAnd() {
        db?.getCollection("test") {
            insert(documentOf("a" to "lorem", "b" to 1),
                documentOf("a" to "dog", "b" to 2))

            var cursor = find(("a" regex "[a-z]+") and ("b" eq 1))
            assertEquals(cursor.size(), 1)

            cursor = find(("a" regex "[a-z]+") and ("b" within 1..2))
            assertEquals(cursor.size(), 2)
        }
    }

    @Test
    fun testOr() {
        db?.getCollection("test") {
            insert(documentOf("a" to "lorem", "b" to 1),
                documentOf("a" to "dog", "b" to 2))

            var cursor = find(("a" eq "lorem") or ("b" eq 2))
            assertEquals(cursor.size(), 2)

            cursor = find(("a" eq "cat") or ("a" eq "wolf"))
            assertEquals(cursor.size(), 0)

            cursor = find(("a" eq "cat") or ("a" eq "wolf") or ("b" eq 2))
            assertEquals(cursor.size(), 1)
        }
    }

    @Test
    fun testNot() {
        db?.getCollection("test") {
            insert(documentOf("a" to 1), documentOf("a" to 5))

            var cursor = find("a" within arrayOf(1, 2, 5))
            assertEquals(cursor.size(), 2)

            cursor = find(!("a" within 1..5))
            assertEquals(cursor.size(), 0)

            cursor = find(!("a" within 6..10))
            assertEquals(cursor.size(), 2)
        }
    }

    @Test
    fun testAll() {
        db?.getCollection("test") {
            insert(documentOf("a" to 1), documentOf("a" to 5))

            val cursor = find()
            assertEquals(cursor.size(), 2)
        }
    }

    @Test
    fun testIntersects() {
        val reader = WKTReader()
        val search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))")

        db?.getCollection("test") {
            val doc1 = documentOf("key" to 1L).put("location", reader.read("POINT(500 505)"))
            val doc2 = documentOf("key" to 2L).put("location", reader.read("LINESTRING(550 551, 525 512, 565 566)"))
            val doc3 = documentOf("key" to 3L).put("location", reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))"))
            insert(doc1, doc2, doc3)

            createIndex("location", IndexOptions.indexOptions(SpatialIndexer.SpatialIndex))

            val cursor1 = find("location" intersects search)
            assertEquals(cursor1.size(), 2)
            assertEquals(cursor1.toList().map { trimMeta(it) }, listOf(doc1, doc2))
        }
    }

    @Test
    fun testGeoWithin() {
        val reader = WKTReader()
        val search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))")

        db?.getCollection("test") {
            val doc1 = documentOf("key" to 1L).put("location", reader.read("POINT(500 505)"))
            val doc2 = documentOf("key" to 2L).put("location", reader.read("LINESTRING(550 551, 525 512, 565 566)"))
            val doc3 = documentOf("key" to 3L).put("location", reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))"))
            insert(doc1, doc2, doc3)

            createIndex("location", IndexOptions.indexOptions(SpatialIndexer.SpatialIndex))

            val cursor1 = find("location" within search)
            assertEquals(cursor1.size(), 1)
            assertEquals(cursor1.toList().map { trimMeta(it) }, listOf(doc1))
        }
    }

    @Test
    fun testNearPoint() {
        val reader = WKTReader()
        val search = reader.read("POINT (490 490)") as Point

        db?.getCollection("test") {
            val doc1 = documentOf("key" to 1L).put("location", reader.read("POINT(500 505)"))
            val doc2 = documentOf("key" to 2L).put("location", reader.read("LINESTRING(550 551, 525 512, 565 566)"))
            val doc3 = documentOf("key" to 3L).put("location", reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))"))
            insert(doc1, doc2, doc3)

            createIndex("location", IndexOptions.indexOptions(SpatialIndexer.SpatialIndex))

            val cursor1 = find("location".near(search, 20.0))
            assertEquals(cursor1.size(), 1)
            assertEquals(cursor1.toList().map { trimMeta(it) }, listOf(doc1))
        }
    }

    @Test
    fun testNearCoordinate() {
        val reader = WKTReader()
        val search = reader.read("POINT (490 490)") as Point
        val coordinate = search.coordinate

        db?.getCollection("test") {
            val doc1 = documentOf("key" to 1L).put("location", reader.read("POINT(500 505)"))
            val doc2 = documentOf("key" to 2L).put("location", reader.read("LINESTRING(550 551, 525 512, 565 566)"))
            val doc3 = documentOf("key" to 3L).put("location", reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))"))
            insert(doc1, doc2, doc3)

            createIndex("location", IndexOptions.indexOptions(SpatialIndexer.SpatialIndex))

            val cursor1 = find("location".near(coordinate, 20.0))
            assertEquals(cursor1.size(), 1)
            assertEquals(cursor1.toList().map { trimMeta(it) }, listOf(doc1))
        }
    }

    @Test
    fun testBetweenFilter() {
        @Getter
        class TestData(private val age: Date)

        val data1 = TestData(GregorianCalendar(2020, Calendar.JANUARY, 11).time)
        val data2 = TestData(GregorianCalendar(2021, Calendar.FEBRUARY, 12).time)
        val data3 = TestData(GregorianCalendar(2022, Calendar.MARCH, 13).time)
        val data4 = TestData(GregorianCalendar(2023, Calendar.APRIL, 14).time)
        val data5 = TestData(GregorianCalendar(2024, Calendar.MAY, 15).time)
        val data6 = TestData(GregorianCalendar(2025, Calendar.JUNE, 16).time)
        val repository = db!!.getRepository(TestData::class.java)
        repository.insert(data1, data2, data3, data4, data5, data6)

        var cursor = repository.find("age".between(
            GregorianCalendar(2020, Calendar.JANUARY, 11).time,
            GregorianCalendar(2025, Calendar.JUNE, 16).time))
        assertEquals(cursor.size(), 6)

        cursor = repository.find("age".between(
            GregorianCalendar(2020, Calendar.JANUARY, 11).time,
            GregorianCalendar(2025, Calendar.JUNE, 16).time, false))
        assertEquals(cursor.size(), 4)

        cursor = repository.find("age".between(
            GregorianCalendar(2020, Calendar.JANUARY, 11).time,
            GregorianCalendar(2025, Calendar.JUNE, 16).time,
            lowerInclusive = true, upperInclusive = false))
        assertEquals(cursor.size(), 5)
    }

    private fun trimMeta(document: Document): Document {
        document.remove(Constants.DOC_ID)
        document.remove(Constants.DOC_REVISION)
        document.remove(Constants.DOC_MODIFIED)
        document.remove(Constants.DOC_SOURCE)
        return document
    }
}