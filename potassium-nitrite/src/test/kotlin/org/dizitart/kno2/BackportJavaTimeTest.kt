package org.dizitart.kno2

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import org.dizitart.no2.IndexType
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices
import org.junit.Test
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
    private val dbPath = getRandomTempDbFile()

    @Indices(value = [(Index(value = "time", type = IndexType.NonUnique))])
    data class TestData (
        @Id val id: String = UUID.randomUUID().toString(),
        val time: LocalDateTime
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
            path = dbPath
            nitriteMapper = object : KNO2JacksonMapper(TestFacade()) {}
        }

        val repo = db.getRepository<TestData>()
        val testData = TestData(time = LocalDateTime.now())
        repo.insert(testData)
        println(repo.find().firstOrDefault())

        Files.delete(Paths.get(dbPath))
    }

    class TestFacade : KNO2JacksonFacade() {
        override fun createObjectMapper(): ObjectMapper {
            val objectMapper = super.createObjectMapper()
            objectMapper.registerModule(ThreeTenAbpModule())
            return objectMapper;
        }
    }
}