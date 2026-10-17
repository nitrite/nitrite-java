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

import org.dizitart.no2.mvstore.MVStoreModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Kotlin stdlib collections stored in a document must be readable after
 * reopening the database with the default deserialization filter (#1330).
 */
class KotlinCollectionsTest : BaseTest() {

    @Test
    fun testReadKotlinCollectionsAfterReopen() {
        db = nitrite {
            loadModule(MVStoreModule(fileName))
            loadModule(KNO2Module())
        }
        db!!.getCollection("test") {
            insert(
                documentOf(
                    "key" to 1,
                    "emptyList" to emptyList<Any>(),
                    "emptySet" to emptySet<Any>(),
                    "emptyMap" to emptyMap<String, Any>(),
                    "nested" to listOf<Collection<Any>>(emptyList(), emptySet()),
                )
            )
        }
        db!!.close()

        db = nitrite {
            loadModule(MVStoreModule(fileName))
            loadModule(KNO2Module())
        }
        db!!.getCollection("test") {
            val doc = find().firstOrNull()
            assertNotNull(doc)
            assertEquals(1, doc!!["key"])
            assertEquals(emptyList<Any>(), doc["emptyList"])
            assertEquals(emptySet<Any>(), doc["emptySet"])
            assertEquals(emptyMap<String, Any>(), doc["emptyMap"])
            assertEquals(listOf<Collection<Any>>(emptyList(), emptySet()), doc["nested"])
        }
    }
}
