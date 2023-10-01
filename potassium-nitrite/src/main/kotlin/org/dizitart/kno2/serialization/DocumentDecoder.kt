package org.dizitart.kno2.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.findPolymorphicSerializer
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.internal.NamedValueDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.dizitart.kno2.isEmpty
import org.dizitart.no2.collection.Document

/**
 * @suppress
 * @author Joris Jensen
 * @since 4.2.0
 */
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal class DocumentDecoder(private val document: Document, descriptor: SerialDescriptor) :
    NamedValueDecoder() {
    private var currentIndex = 0
    private val isCollection = descriptor.kind == StructureKind.LIST || descriptor.kind == StructureKind.MAP
    private val size = if (isCollection) Int.MAX_VALUE else descriptor.elementsCount
    override fun decodeTaggedValue(tag: String): Any = document[tag]

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (currentIndex < size) {
            val name = descriptor.getTag(currentIndex++)
            if (document.containsKey(name)) return currentIndex - 1
            if (isCollection) {
                // if document does not contain key we look for, then indices in collection have ended
                break
            }
        }
        return CompositeDecoder.DECODE_DONE
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int =
        descriptor.elementsCount

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        if (deserializer is AbstractPolymorphicSerializer<*>) {
            val type = document.get(nested("type"))?.toString()
            val actualSerializer: DeserializationStrategy<Any> = deserializer.findPolymorphicSerializer(this, type)

            @Suppress("UNCHECKED_CAST")
            return actualSerializer.deserialize(this) as T
        }

        return deserializer.deserialize(this)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        currentTagOrNull?.let {
            val fieldValue = document[it]
                ?: throw SerializationException("parameter $it should be of type Document, but is null")
            val exceptionMessage = "parameter $it should be of type ${Document::class}, but is ${fieldValue::class}"
            when (descriptor.kind) {
                StructureKind.CLASS, StructureKind.OBJECT -> {
                    val innerDocument = fieldValue as? Document
                        ?: throw SerializationException(exceptionMessage)
                    return DocumentDecoder(innerDocument, descriptor)
                }

                StructureKind.MAP -> {
                    @Suppress("UNCHECKED_CAST")
                    val innerMap = fieldValue as? Map<Any, Any>
                        ?: throw SerializationException(exceptionMessage)
                    return MapDecoder(innerMap.toMutableMap())
                }

                StructureKind.LIST -> {
                    @Suppress("UNCHECKED_CAST")
                    val innerList = fieldValue as? Collection<Any>
                        ?: throw SerializationException(exceptionMessage)
                    return ListDecoder(ArrayDeque(innerList))
                }

                else -> {}
            }
        }
        return super.beginStructure(descriptor)
    }

    override fun decodeNotNullMark(): Boolean =
        currentTagOrNull != null && document.containsKey(currentTag) && document[currentTag] != null

    override fun decodeTaggedEnum(tag: String, enumDescriptor: SerialDescriptor): Int =
        when (val taggedValue = document.get(tag)) {
            is Int -> taggedValue
            is String -> enumDescriptor.getElementIndex(taggedValue)
                .also { if (it == CompositeDecoder.UNKNOWN_NAME) throw SerializationException("Enum '${enumDescriptor.serialName}' does not contain element with name '$taggedValue'") }

            else -> throw SerializationException("Value of enum entry '$tag' is neither an Int nor a String")
        }

    companion object {
        fun <T : Any> decodeFromDocument(source: Document, type: Class<T>) =
            type.kotlin.serializer().run {
                if (source.isEmpty()) {
                    deserialize(EmptyDecoder(descriptor))
                } else {
                    deserialize(DocumentDecoder(source, descriptor))
                }
            }
    }
}


@OptIn(ExperimentalSerializationApi::class)
private class ListDecoder(private val list: ArrayDeque<Any>) : AbstractDecoder() {
    private var elementIndex = 0

    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun decodeValue(): Any = list.removeFirst()

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (list.isEmpty() || elementIndex == descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = descriptor.elementsCount

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (descriptor.kind == StructureKind.CLASS || descriptor.kind == StructureKind.OBJECT || descriptor.kind == StructureKind.MAP) {
            if (list.isEmpty()) {
                return ListDecoder(list)
            }
            val fieldValue = list.removeFirst()
            val innerDocument = fieldValue as? Document
                ?: throw SerializationException("element should be of type ${Document::class}, but is ${fieldValue::class}")
            return DocumentDecoder(innerDocument, descriptor)
        }
        return ListDecoder(list)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class MapDecoder(map: MutableMap<Any, Any>) : AbstractDecoder() {
    val mapEntries = ArrayDeque(map.entries)
    override val serializersModule: SerializersModule = EmptySerializersModule()
    private var elementIndex = 0
    lateinit var currentEntry: Pair<Any, Any>

    override fun decodeValue(): Any = if (elementIndex % 2 == 1) {
        currentEntry = mapEntries.removeFirst().toPair()
        currentEntry.first
    } else {
        currentEntry.second
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (mapEntries.isEmpty() && elementIndex % 2 == 0) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class EmptyDecoder(descriptor: SerialDescriptor) : Decoder, CompositeDecoder {
    var index = descriptor.elementsCount - 1
    override val serializersModule: SerializersModule = EmptySerializersModule()
    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean = decodeBoolean()
    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte = decodeByte()
    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char = decodeChar()
    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double = decodeDouble()

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = when (descriptor.kind) {
        StructureKind.LIST,
        StructureKind.MAP -> CompositeDecoder.DECODE_DONE
        else -> index--
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float = decodeFloat()
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder = decodeInline(descriptor)
    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int = decodeInt()
    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long = decodeLong()

    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? = null

    @OptIn(InternalSerializationApi::class)
    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T = when (deserializer) {
        is SealedClassSerializer -> deserializer.baseClass.sealedSubclasses.first().serializer()
            .deserialize(EmptyDecoder(descriptor))

        else -> decodeSerializableValue(deserializer)
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short = decodeShort()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String = decodeString()

    override fun endStructure(descriptor: SerialDescriptor) {}

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = EmptyDecoder(descriptor)

    override fun decodeBoolean(): Boolean = true
    override fun decodeByte(): Byte = 0
    override fun decodeShort(): Short = 0
    override fun decodeInt(): Int = 0
    override fun decodeLong(): Long = 0
    override fun decodeFloat(): Float = 0f
    override fun decodeDouble(): Double = 0.0
    override fun decodeChar(): Char = '0'
    override fun decodeString(): String = ""
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = 0

    override fun decodeInline(descriptor: SerialDescriptor): Decoder = EmptyDecoder(descriptor)

    override fun decodeNotNullMark(): Boolean = false

    override fun decodeNull(): Nothing? = null
}
