package test.coroutines

import kotlin.coroutines.experimental.RestrictsSuspension

@RestrictsSuspension
interface IteratorBuilder<in T> {
    suspend fun yield(value: T)
    suspend fun yieldAll(value: Collection<T>) {
        if (value.isEmpty()) return
        yieldAll(value as Iterable<T>)
    }

    suspend fun yieldAll(value: Iterable<T>)
}