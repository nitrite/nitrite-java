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
import org.dizitart.no2.common.util.DocumentUtils.isSimilar
import org.junit.Assert.*
import org.junit.Test

/**
 *
 * @author Anindya Chatterjee
 */
class DocumentTest : BaseTest() {

    @Test
    fun testEmptyDocument() {
        val doc = emptyDocument()
        assertTrue(doc.isEmpty())
        assertNotNull(doc.id)
        val doc2 = emptyDocument()
        assertNotEquals(doc, doc2)
    }

    @Test
    fun testDocumentOf1() {
        val doc = documentOf()
        assertTrue(doc.isEmpty())
        assertNotNull(doc.id)
        val doc2 = documentOf()
        assertNotEquals(doc, doc2)
    }

    @Test
    fun testDocumentOf2() {
        val doc = documentOf("a" to 1)
        assertTrue(doc.isNotEmpty())
        assertEquals(doc.size(), 1)
        assertNotNull(doc.id)
        assertEquals(doc.size(), 2)
    }

    @Test
    fun testDocumentOf3() {
        val doc = documentOf("a" to 1, "b" to 2, "c" to 3)
        assertTrue(doc.isNotEmpty())
        assertEquals(doc.size(), 3)
        doc.source
    }

    @Test
    fun documentInsert() {
        db = nitrite()
        val doc = documentOf("a" to 1)
        db?.getCollection("test") {
            insert(doc)
            val cursor = find("a" eq 1)
            assertTrue(isSimilar(cursor.firstOrNull(), doc, "a"))
        }
    }
}