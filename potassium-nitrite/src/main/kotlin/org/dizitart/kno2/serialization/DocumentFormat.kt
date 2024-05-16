@file:OptIn(ExperimentalSerializationApi::class)

package org.dizitart.kno2.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.dizitart.kno2.component1
import org.dizitart.kno2.component2
import org.dizitart.no2.collection.Document
import org.dizitart.no2.exceptions.ObjectMappingException


/**
 * The main entry point to work with Nitrite [Document] serialization and deserialization.
 * It is typically used by constructing an application-specific instance, with configured serialization behavior
 * and, if necessary, registered custom serializers in the [SerializersModule].
 *
 * A `DocumentFormat` instance can be configured in its factory function using the builder pattern:
 *
 * ```
 * val format = DocumentFormat {
 *     // Configuration options (e.g., handling polymorphic classes)
 * }
 * ```
 *
 * For simple use cases or demonstrations, the [DocumentFormat.Default] companion object can be used directly.
 *
 * Once constructed, the instance can be used to encode Kotlin objects into [Document]s and decode [Document]s back into objects.
 *
 * Example of usage:
 * ```
 * @Serializable
 * data class MyData(val id: Int, val name: String)
 *
 * val myData = MyData(1, "Example")
 *
 * // Encoding
 * val document = DocumentFormat.encodeToDocument(myData)
 *
 * // Decoding
 * val decodedData = DocumentFormat.decodeFromDocument<MyData>(document)
 * ```
 *
 * The `DocumentFormat` instance also exposes its `serializersModule`, which can be used in custom serializers.
 */
sealed class DocumentFormat(
    val configuration: DocumentFormatConfiguration,
    override val serializersModule: SerializersModule,
) : SerialFormat {

    private val json = configuration.toJson()

    companion object Default : DocumentFormat(DocumentFormatConfiguration.Default, EmptySerializersModule())

    /**
     * Encodes the given [value] into an equivalent Nitrite [Document] using the specified [serializer].
     *
     * @throws ObjectMappingException if the given value cannot be converted to a Document.
     */
    fun <T : Any> encodeToDocument(serializer: KSerializer<T>, value: T): Document =
        when (val jsonElement = json.encodeToJsonElement(serializer, value)) {
            is JsonObject -> jsonElement.toDocument()
            else -> throw ObjectMappingException("Can't convert object of type `${value::class.qualifiedName}` to Document")
        }

    /**
     * Decodes a Nitrite [Document] into a value of type [T] using the specified [serializer].
     */
    fun <T : Any> decodeFromDocument(serializer: KSerializer<T>, document: Document): T =
        json.decodeFromJsonElement(serializer, document.toJsonObject())

    internal class Custom(
        configuration: DocumentFormatConfiguration,
        serializersModule: SerializersModule,
    ) : DocumentFormat(configuration, serializersModule)

}

/**
 * Builder function to create a [DocumentFormat] instance with a custom configuration.
 *
 * @param from The base [DocumentFormat] to start with (defaults to [DocumentFormat.Default]).
 * @param configure A lambda function to configure the [DocumentFormatBuilder].
 * @return A new [DocumentFormat] instance with the specified configuration.
 */
fun DocumentFormat(
    from: DocumentFormat = DocumentFormat.Default,
    configure: DocumentFormatBuilder.() -> Unit,
): DocumentFormat {
    val (configuration, serializersModule) =
        DocumentFormatBuilder(from.configuration).apply(configure).build()
    return DocumentFormat.Custom(configuration, serializersModule)
}

/**
 * Encodes the given [value] of type [T] into a Nitrite [Document] using the serializer registered in the [serializersModule].
 */
inline fun <reified T : Any> DocumentFormat.encodeToDocument(value: T): Document =
    encodeToDocument(serializersModule.serializer<T>(), value)

/**
 * Decodes the given Nitrite [document] into a value of type [T] using the serializer registered in the [serializersModule].
 */
inline fun <reified T> DocumentFormat.decodeFromDocument(document: Document): T =
    decodeFromDocument(serializersModule.serializer(), document)

private fun Document.toJsonObject() =
    JsonObject(associate { (key, value) -> key to value.toJsonElement() })

private fun Any?.toJsonElement(): JsonElement = when (this) {
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Document -> toJsonObject()
    is List<*> -> JsonArray(map { it.toJsonElement() })
    is Map<*, *> -> JsonObject(checkKeysAreString().mapValues { (_, value) -> value.toJsonElement() })
    is Enum<*> -> JsonPrimitive(this.name)
    null -> JsonNull
    else -> throw ObjectMappingException("Can't convert object of type " + this::class.java + " to JSON")
}

private fun <K, V> Map<out K, V>.checkKeysAreString(): Map<String, V> = mapKeys { (key, _) ->
    when (key) {
        is String -> key
        else -> throw ObjectMappingException("Map keys must be of type String")
    }
}

private fun JsonElement.toDocument(): Any? = when (this) {
    is JsonObject -> toDocument()
    is JsonArray -> toDocument()
    is JsonNull -> null
    is JsonPrimitive -> toDocument()
}

private fun JsonObject.toDocument(): Document {
    val document = Document.createDocument()
    for ((key, value) in entries) {
        document.put(key, value.toDocument(), true)
    }
    return document
}

private fun JsonArray.toDocument(): List<Any?> {
    val list = mutableListOf<Any?>()
    for (element in this) {
        list.add(element.toDocument())
    }
    return list
}

private fun JsonPrimitive.toDocument(): Any = when {
    isString -> content
    else -> content.toIntOrNull()
        ?: content.toLongOrNull()
        ?: content.toDoubleOrNull()
        ?: content.toFloatOrNull()
        ?: content.toBoolean()
}