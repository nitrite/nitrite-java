@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSerializationApi::class)

package org.dizitart.kno2.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/**
 * Configuration for document format.
 *
 * @property encodeDefaults    Indicates whether to encode default property values.
 * @property ignoreUnknownKeys Indicates whether to ignore unknown keys when decoding.
 * @property isLenient         Indicates whether to apply a lenient parsing strategy.
 * @property allowStructuredMapKeys    Indicates whether to allow structured map keys.
 * @property explicitNulls     Indicates whether to encode explicit nulls.
 * @property coerceInputValues Indicates whether to coerce input values.
 * @property useArrayPolymorphism  Indicates whether to enable array polymorphism.
 * @property classDiscriminator    The class discriminator key.
 * @property allowSpecialFloatingPointValues  Indicates whether to allow special floating point values.
 * @property useAlternativeNames  Indicates whether to use alternative names.
 * @property namingStrategy     The naming strategy for properties.
 * @property decodeEnumsCaseInsensitive    Indicates whether to decode enums case insensitive.
 */
class DocumentFormatConfiguration(
    val encodeDefaults: Boolean = false,
    val ignoreUnknownKeys: Boolean = true,
    val isLenient: Boolean = false,
    val allowStructuredMapKeys: Boolean = false,
    val explicitNulls: Boolean = true,
    val coerceInputValues: Boolean = false,
    val useArrayPolymorphism: Boolean = false,
    val classDiscriminator: String = "type",
    val allowSpecialFloatingPointValues: Boolean = false,
    val useAlternativeNames: Boolean = true,
    val namingStrategy: JsonNamingStrategy? = null,
    val decodeEnumsCaseInsensitive: Boolean = false,
) {
    companion object {
        val Default = DocumentFormatConfiguration()
    }
}

internal fun DocumentFormatConfiguration.toJson() = Json {
    encodeDefaults = this@toJson.encodeDefaults
    ignoreUnknownKeys = this@toJson.ignoreUnknownKeys
    isLenient = this@toJson.isLenient
    allowStructuredMapKeys = this@toJson.allowStructuredMapKeys
    explicitNulls = this@toJson.explicitNulls
    coerceInputValues = this@toJson.coerceInputValues
    useArrayPolymorphism = this@toJson.useArrayPolymorphism
    classDiscriminator = this@toJson.classDiscriminator
    allowSpecialFloatingPointValues = this@toJson.allowSpecialFloatingPointValues
    useAlternativeNames = this@toJson.useAlternativeNames
    namingStrategy = this@toJson.namingStrategy
    decodeEnumsCaseInsensitive = this@toJson.decodeEnumsCaseInsensitive

}