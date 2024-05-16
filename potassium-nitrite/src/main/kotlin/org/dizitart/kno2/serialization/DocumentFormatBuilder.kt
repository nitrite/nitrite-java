@file:OptIn(ExperimentalSerializationApi::class)

package org.dizitart.kno2.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

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

    data class Built(
        val configuration: DocumentFormatConfiguration,
        val serializersModule: SerializersModule,
    )

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