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
import kotlinx.serialization.Serializable
import org.dizitart.kno2.serialization.KotlinXSerializationMapper
import org.dizitart.no2.collection.Document
import org.dizitart.no2.mvstore.MVStoreModule
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

/**
 *
 * @author Joris Jensen
 */
class KotlinXSerializationMapperTest {
    private val dbPath = getRandomTempDbFile()

    @Serializable
    private data class TestData(
        val polymorphType: SomePolymorphType,
        val someMap: Map<String, String>,
        val someSerializableObjectMap: Map<InnerObject, SomePolymorphType>,
        val innerObject: InnerObject,
        val id: String,
        val valueClass: SomeValueClass,
        val someInt: Int,
        val someDouble: Double,
        val nullable: String?,
        val enum: SomeEnum,
        val someList: List<TestData>,
        val someArray: Array<String>,
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
        sealed class SomePolymorphType(val value: String) {
            @Serializable
            object SomeTypeA : SomePolymorphType("Type A")

            @Serializable
            data class SomeTypeB(val someValue: String) : SomePolymorphType("Type B")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestData

            if (polymorphType != other.polymorphType) return false
            if (someMap != other.someMap) return false
            if (innerObject != other.innerObject) return false
            if (id != other.id) return false
            if (someInt != other.someInt) return false
            if (someDouble != other.someDouble) return false
            if (nullable != other.nullable) return false
            if (enum != other.enum) return false
            if (someList != other.someList) return false
            return someArray.contentEquals(other.someArray)
        }

        override fun hashCode(): Int {
            var result = polymorphType.hashCode()
            result = 31 * result + someMap.hashCode()
            result = 31 * result + innerObject.hashCode()
            result = 31 * result + id.hashCode()
            result = 31 * result + someInt
            result = 31 * result + someDouble.hashCode()
            result = 31 * result + (nullable?.hashCode() ?: 0)
            result = 31 * result + enum.hashCode()
            result = 31 * result + someList.hashCode()
            result = 31 * result + someArray.contentHashCode()
            return result
        }
    }

    private val testData = TestData(
        TestData.SomePolymorphType.SomeTypeA,
        someList = listOf(
            TestData(
                TestData.SomePolymorphType.SomeTypeB("someValue"),
                emptyMap(),
                emptyMap(),
                TestData.InnerObject(""),
                "",
                TestData.SomeValueClass("someString"),
                1,
                1.0,
                "test",
                TestData.SomeEnum.SomeValue,
                emptyList(),
                emptyArray(),
            ),
        ),
        valueClass = TestData.SomeValueClass("someString"),
        someArray = arrayOf("someArrayData"),
        id = "testId",
        someInt = 1,
        someDouble = 1.0,
        innerObject = TestData.InnerObject("someValue"),
        nullable = null,
        enum = TestData.SomeEnum.SomeValue,
        someMap = mapOf("testkey" to "testvalue", "test1" to "test2"),
        someSerializableObjectMap = mapOf(TestData.InnerObject("test") to TestData.SomePolymorphType.SomeTypeA)
    )

    @Test
    fun testModule() {
        val db = nitrite {
            loadModule(MVStoreModule(dbPath))
            loadModule(KotlinXSerializationMapper)
        }

        val repo = db.getRepository<TestData>()
        repo.insert(testData)
        repo.find { a -> a.second.get("id") == testData.id }.firstOrNull().also {
            assertEquals(it, testData)
        }
        db.close()
        Files.delete(Paths.get(dbPath))
    }

    @Test
    fun testMapping() {
        val document = KotlinXSerializationMapper.tryConvert(testData, Document::class.java)
        val decodedObject = KotlinXSerializationMapper.tryConvert(document, TestData::class.java) as TestData
        assertSame(testData.someArray.size, decodedObject.someArray.size)
        testData.someArray.forEachIndexed { index, s -> assertEquals(decodedObject.someArray[index], s) }
        assertEquals(testData, decodedObject.copy(someArray = testData.someArray))
    }
}