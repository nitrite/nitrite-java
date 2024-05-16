@file:OptIn(ExperimentalSerializationApi::class)

package org.dizitart.kno2.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * The DocumentFormatBuilder class is used to build a configuration for document formatting.
 *
 * @property encodeDefaults Whether to encode default values during serialization.
 * @property ignoreUnknownKeys Whether to ignore unknown keys during deserialization.
 * @property isLenient Whether to enable lenient parsing of JSON input.
 * @property allowStructuredMapKeys Whether to allow structured map keys during serialization.
 * @property explicitNulls Whether to include null values in the serialized output.
 * @property coerceInputValues Whether to coerce input values during deserialization.
 * @property useArrayPolymorphism Whether to use array polymorphism during serialization.
 * @property classDiscriminator The name of the class discriminator property to use during serialization and deserialization.
 * @property allowSpecialFloatingPointValues Whether to allow special floating-point values during deserialization.
 * @property useAlternativeNames Whether to use alternative property names during serialization and deserialization.
 * @property namingStrategy The JSON naming strategy to use during serialization and deserialization.
 * @property decodeEnumsCaseInsensitive Whether to decode enums case-insensitively during deserialization.
 * @property serializersModule The module containing custom serializers and deserializers.
 */
class DocumentFormatBuilder internal constructor(from: DocumentFormatConfiguration = DocumentFormatConfiguration.Default) {
    var encodeDefaults: Boolean = from.encodeDefaults
    var ignoreUnknownKeys: Boolean = from.ignoreUnknownKeys
    var isLenient: Boolean = from.isLenient
    var allowStructuredMapKeys: Boolean = from.allowStructuredMapKeys
    var explicitNulls: Boolean = from.explicitNulls
    var coerceInputValues: Boolean = from.coerceInputValues
    var useArrayPolymorphism: Boolean = from.useArrayPolymorphism
    var classDiscriminator: String = from.classDiscriminator
    var allowSpecialFloatingPointValues: Boolean = from.allowSpecialFloatingPointValues
    var useAlternativeNames: Boolean = from.useAlternativeNames
    var namingStrategy: JsonNamingStrategy? = from.namingStrategy
    var decodeEnumsCaseInsensitive: Boolean = from.decodeEnumsCaseInsensitive
    var serializersModule: SerializersModule = EmptySerializersModule()

    /**
     * Represents a built object with the specified configuration settings and serializers module.
     *
     * @property configuration the document format configuration settings
     * @property serializersModule the module containing custom serializers and deserializers
     */
    data class Built(
        val configuration: DocumentFormatConfiguration,
        val serializersModule: SerializersModule,
    )

    /**
     * Builds a `Built` object with the specified configuration settings and serializers module.
     *
     * @return a `Built` object containing the configuration and serializers module
     *
     * @property encodeDefaults whether to encode default values during serialization
     * @property ignoreUnknownKeys whether to ignore unknown keys during deserialization
     * @property isLenient whether to enable lenient parsing of JSON input
     * @property allowStructuredMapKeys whether to allow structured map keys during serialization
     * @property explicitNulls whether to include null values in the serialized output
     * @property coerceInputValues whether to coerce input values during deserialization
     * @property useArrayPolymorphism whether to use array polymorphism during serialization
     * @property classDiscriminator the name of the class discriminator property to use during serialization and deserialization
     * @property allowSpecialFloatingPointValues whether to allow special floating-point values during deserialization
     * @property useAlternativeNames whether to use alternative property names during serialization and deserialization
     * @property namingStrategy the JSON naming strategy to use during serialization and deserialization
     * @property decodeEnumsCaseInsensitive whether to decode enums case-insensitively during deserialization
     * @property serializersModule the module containing custom serializers and deserializers
     */
    fun build() = Built(
        DocumentFormatConfiguration(
            encodeDefaults = encodeDefaults,
            ignoreUnknownKeys = ignoreUnknownKeys,
            isLenient = isLenient,
            allowStructuredMapKeys = allowStructuredMapKeys,
            explicitNulls = explicitNulls,
            coerceInputValues = coerceInputValues,
            useArrayPolymorphism = useArrayPolymorphism,
            classDiscriminator = classDiscriminator,
            allowSpecialFloatingPointValues = allowSpecialFloatingPointValues,
            useAlternativeNames = useAlternativeNames,
            namingStrategy = namingStrategy,
            decodeEnumsCaseInsensitive = decodeEnumsCaseInsensitive,
        ),
        serializersModule
    )
}