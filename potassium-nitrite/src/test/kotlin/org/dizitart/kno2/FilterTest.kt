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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

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

            cursor = find(("a" regex "[a-z]+") and ("b" within  1..2))
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
}