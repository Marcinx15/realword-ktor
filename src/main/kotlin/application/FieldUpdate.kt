package com.example.application

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