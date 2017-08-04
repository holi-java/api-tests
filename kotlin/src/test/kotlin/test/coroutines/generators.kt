package test.coroutines

import kotlin.coroutines.experimental.RestrictsSuspension

@RestrictsSuspension
interface IteratorBuilder<in T> {
    suspend fun yield(value: T)
    suspend fun yieldAll(value: Collection<T>) {
        if (!value.isEmpty()) yieldAll(value.iterator())
    }

    suspend fun yieldAll(value: Iterable<T>) = yieldAll(value.iterator())

    suspend fun yieldAll(value: Iterator<T>)
}