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

import org.dizitart.no2.exceptions.SecurityException
import org.dizitart.no2.index.fulltext.EnglishTextTokenizer
import org.dizitart.no2.index.fulltext.UniversalTextTokenizer
import org.dizitart.no2.mapper.GenericMapper
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 *
 * @author Anindya Chatterjee
 */

class BuilderTest : BaseTest() {

    @Test
    fun testBuilder1() {
        db = nitrite {
            path = fileName
            autoCommit = false
        }

        val context = db?.context!!
        assertEquals(context.filePath, fileName)
        assertEquals(context.autoCommitBufferSize, 1024)
        assertFalse(context.isAutoCommitEnabled)
        assertFalse(context.isReadOnly)
        assertTrue(context.isAutoCompactEnabled)
        assertFalse(context.isCompressed)
        assertTrue(context.nitriteMapper is KNO2JacksonMapper)
        assertNull(context.textIndexer)
        assertTrue(context.textTokenizer is EnglishTextTokenizer)

        assertFalse(db?.isClosed!!)

        val collection = db?.getCollection("test")!!
        assertFalse(collection.isClosed)
    }

    @Test
    fun testBuilder2() {
        db = nitrite {
            file = File(fileName)
            autoCommitBufferSize = 2048
            compress = true
            autoCompact = false
            textTokenizer = UniversalTextTokenizer()
            nitriteMapper = KNO2JacksonMapper()
            disableShutdownHook = true
        }

        val context = db?.context!!
        assertEquals(context.filePath, fileName)
        assertEquals(context.autoCommitBufferSize, 2048)
        assertTrue(context.isAutoCommitEnabled)
        assertFalse(context.isReadOnly)
        assertFalse(context.isAutoCompactEnabled)
        assertTrue(context.isCompressed)
        assertTrue(context.nitriteMapper is KNO2JacksonMapper)
        assertTrue(context.nitriteMapper is GenericMapper)
        assertNull(context.textIndexer)
        assertTrue(context.textTokenizer is UniversalTextTokenizer)

        assertFalse(db?.isClosed!!)
    }

    @Test(expected = SecurityException::class)
    fun testBuilderNoUser() {
        db = nitrite("", "password") {
            file = File(fileName)
        }
    }

    @Test(expected = SecurityException::class)
    fun testBuilderNoPassword() {
        db = nitrite("user", "") {
            file = File(fileName)
        }
    }

    @Test
    fun testBuilderNoUserPassword() {
        db = nitrite("", "") {
            file = File(fileName)
        }
    }

    @Test
    fun testBuilderInMemory() {
        db = nitrite()
        assertTrue(db?.context?.isInMemory!!)
    }
}

