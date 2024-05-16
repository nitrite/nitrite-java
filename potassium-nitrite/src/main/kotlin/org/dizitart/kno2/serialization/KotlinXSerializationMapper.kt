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

package org.dizitart.kno2.serialization

import java.util.*
import kotlinx.serialization.serializer
import org.dizitart.no2.NitriteConfig
import org.dizitart.no2.collection.Document
import org.dizitart.no2.collection.NitriteId
import org.dizitart.no2.common.mapper.NitriteMapper
import org.dizitart.no2.exceptions.ObjectMappingException

/**
 * A [org.dizitart.no2.common.mapper.NitriteMapper] module that uses KotlinX Serialization
 * for object to [Document] conversion and vice versa.
 *
 * @author Joris Jensen
 * @since 4.2.0
 */
sealed class KotlinXSerializationMapper : NitriteMapper {

    abstract val documentFormat: DocumentFormat

    /**
     * It provides a default implementation for the documentFormat property, which returns the DocumentFormat.Default value.
     *
     * @property documentFormat The document format to be used.
     * @see KotlinXSerializationMapper
     */
    companion object Default : KotlinXSerializationMapper() {
        override val documentFormat: DocumentFormat
            get() = DocumentFormat.Default
    }

    internal class Custom(
        override val documentFormat: DocumentFormat = DocumentFormat.Default,
    ) : KotlinXSerializationMapper()

    private fun <Source : Any> convertToDocument(source: Source): Document =
        documentFormat.encodeToDocument(documentFormat.serializersModule.serializer(source::class.java), source)

    override fun <Source, Target : Any> tryConvert(source: Source, type: Class<Target>): Any? {
        val nonNullSource = source ?: return null
        @Suppress("UNCHECKED_CAST")
        return when {
            isValueType(nonNullSource::class.java) -> source as Target
            Document::class.java.isAssignableFrom(type) -> when (source) {
                is Document -> source
                else -> convertToDocument(source)
            }

            source is Document -> documentFormat.decodeFromDocument(
                serializer = documentFormat.serializersModule.serializer(type),
                document = source
            )

            else -> throw ObjectMappingException("Can't convert object of type " + nonNullSource::class.java + " to type " + type)
        }
    }

    private fun isValueType(type: Class<*>): Boolean {
        if (type.isPrimitive && type != Void.TYPE) return true
        if (valueTypes.contains(type)) return true
        return valueTypes.any { it.isAssignableFrom(type) }
    }

    private val valueTypes: List<Class<*>> = listOf(
        Number::class.java,
        Boolean::class.java,
        Character::class.java,
        String::class.java,
        Array<Byte>::class.java,
        Enum::class.java,
        NitriteId::class.java,
        Date::class.java,
    )

    override fun initialize(nitriteConfig: NitriteConfig) {}
}

/**
 * Creates a [KotlinXSerializationMapper] with the specified [documentFormat].
 *
 * @param documentFormat the document format to be used for object to [Document] conversion and vice versa.
 * @return the created [KotlinXSerializationMapper] instance.
 */
fun KotlinXSerializationMapper(
    documentFormat: DocumentFormat = DocumentFormat.Default,
): KotlinXSerializationMapper = KotlinXSerializationMapper.Custom(documentFormat)

