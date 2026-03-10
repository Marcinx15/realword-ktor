package com.example.routes.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = FieldUpdateSerializer::class)
sealed interface FieldUpdate<out T> {
    fun <R> map(f: (T) -> R): FieldUpdate<R>
    fun forEach(f: (T) -> Unit)

    data object Absent : FieldUpdate<Nothing> {
        override fun <R> map(f: (Nothing) -> R): FieldUpdate<R> = this
        override fun forEach(f: (Nothing) -> Unit) = Unit
    }

    data class Present<T>(val data: T) : FieldUpdate<T> {
        override fun <R> map(f: (T) -> R): FieldUpdate<R> = Present(f(data))
        override fun forEach(f: (T) -> Unit) = f(data)
    }
}

class FieldUpdateSerializer<T>(private val valueSerializer: KSerializer<T>) : KSerializer<FieldUpdate<T>> {
    override val descriptor: SerialDescriptor = valueSerializer.descriptor

    override fun serialize(encoder: Encoder, value: FieldUpdate<T>) {
        when (value) {
            is FieldUpdate.Absent -> throw SerializationException("Cannot serialize Absent")
            is FieldUpdate.Present -> encoder.encodeSerializableValue(valueSerializer, value.data)
        }
    }

    override fun deserialize(decoder: Decoder): FieldUpdate<T> {
        val value = valueSerializer.deserialize(decoder)
        return FieldUpdate.Present(value)
    }
}


