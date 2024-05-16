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

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertSame
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.serialization.DocumentFormat
import org.dizitart.kno2.serialization.KotlinXSerializationMapper
import org.dizitart.kno2.serialization.decodeFromDocument
import org.dizitart.kno2.serialization.encodeToDocument
import org.dizitart.no2.exceptions.ValidationException
import org.dizitart.no2.mvstore.MVStoreModule
import org.dizitart.no2.repository.annotations.Id
import org.junit.Test
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.test.assertNotNull


/**
 *
 * @author Joris Jensen
 */
class KotlinXSerializationMapperTest {

    private val dbPath = getRandomTempDbFile()

    private val testData = KotlinxTestData(
        polymorphType = KotlinxTestData.SomePolymorphType.SomeTypeA,
        someMap = mapOf("testkey" to "testvalue", "test1" to "test2"),
        someSerializableObjectMap = mapOf(KotlinxTestData.InnerObject("test") to KotlinxTestData.SomePolymorphType.SomeTypeA),
        innerObject = KotlinxTestData.InnerObject("someValue"),
        valueClass = KotlinxTestData.SomeValueClass("someString"),
        someInt = 1,
        someDouble = 1.0,
        nullable = null,
        enum = KotlinxTestData.SomeEnum.SomeValue,
        someList = listOf(
            KotlinxTestData(
                polymorphType = KotlinxTestData.SomePolymorphType.SomeTypeB("someValue"),
                someMap = emptyMap(),
                someSerializableObjectMap = emptyMap(),
                innerObject = KotlinxTestData.InnerObject(""),
                id = null,
                valueClass = KotlinxTestData.SomeValueClass("someString"),
                someInt = 1,
                someDouble = 1.0,
                nullable = "test",
                enum = KotlinxTestData.SomeEnum.SomeValue,
                someList = emptyList(),
                someArray = emptyList(),
            ),
            KotlinxTestData(
                polymorphType = KotlinxTestData.SomePolymorphType.SomeTypeB("someValue"),
                someMap = emptyMap(),
                someSerializableObjectMap = emptyMap(),
                innerObject = KotlinxTestData.InnerObject(""),
                id = null,
                valueClass = KotlinxTestData.SomeValueClass("someString"),
                someInt = 1,
                someDouble = 1.0,
                nullable = "test",
                enum = KotlinxTestData.SomeEnum.SomeValue,
                someList = emptyList(),
                someArray = emptyList(),
            ),
        ),
        someArray = listOf("someArrayData")
    )

    @Test
    fun testModule() {
        val documentFormat = DocumentFormat { allowStructuredMapKeys = true }
        val db = nitrite {
            validateRepositories = false
            loadModule(MVStoreModule(dbPath))
            loadModule(KotlinXSerializationMapper(documentFormat))
        }

        val repo = db.getRepository<KotlinxTestData>()
        repo.insert(testData)
        repo.find()
            .firstOrNull()
            .also { assertNotNull(it) }
        db.close()
        Path(dbPath).deleteIfExists()
    }

    @Test
    fun testMapping() {
        val documentFormat = DocumentFormat { allowStructuredMapKeys = true }
        val document = documentFormat.encodeToDocument(testData)
        val decodedObject = documentFormat.decodeFromDocument<KotlinxTestData>(document)
        assertSame(testData.someArray.size, decodedObject.someArray.size)
        testData.someArray.forEachIndexed { index, s -> assertEquals(decodedObject.someArray[index], s) }
        assertEquals(testData, decodedObject.copy(someArray = testData.someArray))
    }

    @Test(expected = ValidationException::class)
    fun testRepositoryValidationFailsWithKotlinx() {
        val db = nitrite {
            loadModule(MVStoreModule(dbPath))
            loadModule(KotlinXSerializationMapper)
        }

        val repo = db.getRepository<CacheEntry>()
        repo.insert(CacheEntry("sha256"))
        repo.find(CacheEntry::sha256 eq "sha256")
            .firstOrNull()
            .also { assertEquals(it?.sha256, "sha256") }
        db.close()
        Path(dbPath).deleteIfExists()
    }

    @Test
    fun testRepositoryValidationDisabled() {
        val db = nitrite {
            validateRepositories = false
            loadModule(MVStoreModule(dbPath))
            loadModule(KotlinXSerializationMapper)
        }

        val repo = db.getRepository<CacheEntry>()
        repo.insert(CacheEntry("sha256", Clock.System.now()))
        repo.find(CacheEntry::sha256 eq "sha256")
            .firstOrNull()
            .also { assertEquals(it?.sha256, "sha256") }
        db.close()
        Path(dbPath).deleteIfExists()
    }


    @Test(expected = SerializationException::class)
    fun `should fail with deep put enabled`() {
        val documentFormat = DocumentFormat { allowDeepPut = true }
        val document = documentFormat.encodeToDocument(AClass.create())
        documentFormat.decodeFromDocument<AClass>(document)
    }

    @Test(expected = SerializationException::class)
    fun `should succeed with deep put disabled`() {
        val documentFormat = DocumentFormat { allowDeepPut = false }
        val document = documentFormat.encodeToDocument(AClass.create())
        documentFormat.decodeFromDocument<AClass>(document)
    }
}

@Serializable
data class KotlinxTestData(
    val polymorphType: SomePolymorphType,
    val someMap: Map<String, String>,
    val someSerializableObjectMap: Map<InnerObject, SomePolymorphType>,
    val innerObject: InnerObject,
    @Id val id: SerializableNitriteId? = null,
    val valueClass: SomeValueClass,
    val someInt: Int,
    val someDouble: Double,
    val nullable: String?,
    val enum: SomeEnum,
    val someList: List<KotlinxTestData>,
    val someArray: List<String>,
) {

    @JvmInline
    @Serializable
    value class SomeValueClass(val s: String)

    @Serializable
    enum class SomeEnum {
        SomeValue,
    }

    @Serializable
    data class InnerObject(val someValue: String)

    @Serializable
    sealed interface SomePolymorphType {

        val value: String

        @Serializable
        data object SomeTypeA : SomePolymorphType {
            override val value: String
                get() = "Type A"
        }

        @Serializable
        data class SomeTypeB(val someValue: String, override val value: String = "TypeB") : SomePolymorphType
    }
}

@Serializable
data class AClass(
    val aString: String,
    val aMap: Map<String, String>,
) {
    companion object {
        fun create() = AClass(
            aString = "aString",
            aMap = mapOf("the.key.to.split" to "aValue")
        )
    }
}

@Serializable
data class CacheEntry(
    val sha256: String,
    val lastUpdated: Instant = Clock.System.now(),
)