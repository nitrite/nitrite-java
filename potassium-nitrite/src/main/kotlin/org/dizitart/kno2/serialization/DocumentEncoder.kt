package org.dizitart.kno2.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.findPolymorphicSerializer
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.internal.TaggedEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.dizitart.no2.collection.Document
import kotlin.reflect.full.starProjectedType

/**
 * @suppress
 * @author Joris Jensen
 * @since 4.2.0
 */
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal class DocumentEncoder(private val document: Document) : TaggedEncoder<String>() {
    override fun encodeTaggedValue(tag: String, value: Any) {
        document.put(tag, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        if (serializer is AbstractPolymorphicSerializer<*>) {
            val casted = serializer as AbstractPolymorphicSerializer<Any>
            val actualSerializer = casted.findPolymorphicSerializer(this, value as Any)
            val innerDocument = Document.createDocument()
            innerDocument.put("type", actualSerializer.descriptor.serialName)
            document.put(currentTagOrNull, innerDocument)
            return actualSerializer.serialize(DocumentEncoder(innerDocument), value)
        }

        return serializer.serialize(this, value)
    }

    override fun encodeTaggedNull(tag: String) {
        document.put(tag, null)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        currentTagOrNull?.let {
            when (descriptor.kind) {
                StructureKind.CLASS, StructureKind.OBJECT -> {
                    val innerDocument = Document.createDocument()
                    document.put(it, innerDocument)
                    return DocumentEncoder(innerDocument)
                }

                StructureKind.MAP -> {
                    val innerMap = mutableMapOf<Any, Any>()
                    document.put(it, innerMap)
                    return MapEncoder(innerMap)
                }

                StructureKind.LIST -> {
                    val l = mutableListOf<Any>()
                    document.put(it, l)
                    return ListEncoder(l)
                }

                else -> {}
            }
        }
        return super.beginStructure(descriptor)
    }

    override fun SerialDescriptor.getTag(index: Int): String = getElementName(index)

    override fun encodeTaggedEnum(tag: String, enumDescriptor: SerialDescriptor, ordinal: Int) {
        document.put(tag, enumDescriptor.getElementName(ordinal))
    }

    companion object {
        fun <T : Any> encodeToDocument(value: T): Document =
            Document.createDocument().also {
                serializer(value::class.starProjectedType).serialize(DocumentEncoder(it), value)
            }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class ListEncoder(val list: MutableList<Any>) : AbstractEncoder() {

    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun encodeValue(value: Any) {
        list.add(value)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (descriptor.kind == StructureKind.CLASS || descriptor.kind == StructureKind.OBJECT || descriptor.kind == StructureKind.MAP) {
            val innerDocument = Document.createDocument()
            list.add(innerDocument)
            return DocumentEncoder(innerDocument)
        }
        return super.beginStructure(descriptor)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class MapEncoder(val map: MutableMap<Any, Any>) : AbstractEncoder() {
    var currentKey: Any? = null
    var currentIndex = 0

    override val serializersModule: SerializersModule = EmptySerializersModule()
    override fun encodeValue(value: Any) {
        if (currentIndex % 2 == 0) {
            currentKey = value
        } else {
            currentKey?.let {
                map[it] = value
            } ?: throw SerializationException("no key found for value $value")
        }
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        currentIndex = index
        return true
    }
}