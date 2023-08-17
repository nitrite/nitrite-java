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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import lombok.extern.slf4j.Slf4j
import org.dizitart.no2.index.IndexType
import org.dizitart.no2.repository.annotations.Id
import org.dizitart.no2.repository.annotations.Index
import org.dizitart.no2.mvstore.MVStoreModule
import org.junit.Test
import org.slf4j.LoggerFactory
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 *
 * @author Anindya Chatterjee
 */
class BackportJavaTimeTest {
    private val log = LoggerFactory.getLogger(BackportJavaTimeTest::class.java)
    private val dbPath = getRandomTempDbFile()

    @Index(fields = ["time"], type = IndexType.NON_UNIQUE)
    data class TestData(
            @Id val id: String = UUID.randomUUID().toString(),
            val time: LocalDateTime = LocalDateTime.now()
    )

    class ThreeTenAbpModule : SimpleModule() {
        override fun setupModule(context: SetupContext?) {
            addDeserializer(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime>() {
                override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): LocalDateTime? {
                    val timeStamp = p?.longValue
                    return if (timeStamp == -1L || timeStamp == null) null else {
                        LocalDateTime.ofEpochSecond(timeStamp, 0, ZoneOffset.UTC)
                    }
                }
            })

            addSerializer(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime>() {
                override fun serialize(value: LocalDateTime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
                    if (value == null) {
                        gen?.writeNull()
                    } else {
                        val timeStamp = value.toEpochSecond(ZoneOffset.UTC)
                        gen?.writeNumber(timeStamp)
                    }
                }
            })
            super.setupModule(context)
        }
    }

    @Test
    fun testIssue59() {
        val db = nitrite {
            loadModule(MVStoreModule(dbPath))
            loadModule(KNO2Module(ThreeTenAbpModule()))
        }

        val repo = db.getRepository<TestData>()
        val testData = TestData(time = LocalDateTime.now())
        repo.insert(testData)

        try {
            Files.delete(Paths.get(dbPath))
        } catch (e: Exception) {
            log.error("Failed to delete db file", e)
        }
    }
}