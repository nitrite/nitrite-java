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

import org.dizitart.no2.NitriteConfig
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.Constants
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper
import org.dizitart.no2.common.module.NitriteModule.module
import org.dizitart.no2.exceptions.NitriteSecurityException
import org.dizitart.no2.index.NitriteTextIndexer
import org.dizitart.no2.index.fulltext.UniversalTextTokenizer
import org.dizitart.no2.migration.InstructionSet
import org.dizitart.no2.migration.Migration
import org.dizitart.no2.mvstore.MVStoreModule
import org.junit.Assert.*
import org.junit.Test

/**
 *
 * @author Anindya Chatterjee
 */

class BuilderTest : BaseTest() {

    @Test
    fun testBuilder1() {
        db = nitrite {
            loadModule(MVStoreModule(fileName))
        }

        val context = db?.config!!
        assertEquals(context.nitriteStore.storeConfig.filePath(), fileName)
        assertFalse(context.nitriteStore.storeConfig.isReadOnly)
        assertTrue(context.nitriteMapper() is KNO2JacksonMapper)

        assertFalse(db?.isClosed!!)

        val collection = db?.getCollection("test")!!
        assertTrue(collection.isOpen)
    }

    @Test
    fun testBuilder2() {
        db = nitrite {
            loadModule(MVStoreModule(fileName))

            loadModule(module(NitriteTextIndexer(UniversalTextTokenizer())))
        }

        val context = db?.config!!
        assertEquals(context.nitriteStore.storeConfig.filePath(), fileName)
        assertFalse(context.nitriteStore.storeConfig.isReadOnly)
        assertTrue(context.nitriteMapper() is KNO2JacksonMapper)

        assertFalse(db?.isClosed!!)
    }

    @Test(expected = NitriteSecurityException::class)
    fun testBuilderNoUser() {
        db = nitrite("", "password") {
            loadModule(MVStoreModule(fileName))
        }
    }

    @Test(expected = NitriteSecurityException::class)
    fun testBuilderNoPassword() {
        db = nitrite("user", "") {
            loadModule(MVStoreModule(fileName))
        }
    }

    @Test
    fun testBuilderNoUserPassword() {
        db = nitrite("", "") {
            loadModule(MVStoreModule(fileName))
        }
    }

    @Test
    fun testBuilderInMemory() {
        db = nitrite {}
        assertTrue(db?.config?.nitriteStore?.storeConfig?.isInMemory!!)
    }

    @Test
    fun testBuilderSchemaVersion() {
        db = nitrite {
            schemaVersion = 10
        }
        assertEquals(db?.config?.schemaVersion, 10)
    }

    @Test
    fun testBuilderFieldSeparator() {
        db = nitrite {
            fieldSeparator = '/'.toString()
        }
        assertEquals(NitriteConfig.getFieldSeparator(), '/'.toString())
        nitrite {
            fieldSeparator = '.'.toString()
        }
    }

    @Test
    fun testBuilderEntityConverter() {
        val db = nitrite {
            registerEntityConverter(TestClassConverter())
        }

        val repository = db.getRepository<TestClass>()
        val testClass = TestClass().apply {
            id = 1
            name = "test"
        }

        repository.insert(testClass)

        val result = repository.find()
        assertEquals(result.size(), 1)
        assertEquals(result.first()?.id, 1)
        assertEquals(result.first()?.name, "test")
    }

    @Test
    fun testBuilderMigration() {
        var db = nitrite {
            loadModule(MVStoreModule(fileName))
            schemaVersion = 1
        }
        db.getCollection("test").insert(Document.createDocument())
        assertEquals(db.hasCollection("test"), true)
        db.close()

        // add user password
        db = nitrite {
            addMigration(object : Migration(Constants.INITIAL_SCHEMA_VERSION, 2) {
                override fun migrate(instruction: InstructionSet) {
                    instruction.forDatabase()
                        .dropCollection("test")
                }
            })
            loadModule(MVStoreModule(fileName))
            schemaVersion = 2
        }
        db.close()

        db = nitrite {
            loadModule(MVStoreModule(fileName))
            schemaVersion = 2
        }
        assertEquals(db.databaseMetaData.schemaVersion, 2)

        // check if collection is dropped
        assertEquals(db.hasCollection("test"), false)
        db.close()
    }
}

class TestClass {
    var id: Int? = 0
    var name: String? = null
}

class TestClassConverter : EntityConverter<TestClass> {
    override fun getEntityType(): Class<TestClass> {
        return TestClass::class.java
    }

    override fun fromDocument(document: Document?, nitriteMapper: NitriteMapper?): TestClass {
        return TestClass().apply {
            id = document?.get("id") as Int?
            name = document?.get("name", String::class.java)
        }
    }

    override fun toDocument(entity: TestClass?, nitriteMapper: NitriteMapper?): Document {
        return emptyDocument().apply {
            put("id", entity?.id)
            put("name", entity?.name)
        }
    }
}

