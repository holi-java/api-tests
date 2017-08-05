package test.coroutines

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.throws
import org.junit.Test
import java.util.*
import java.util.Collections.*
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.createCoroutine
import kotlin.coroutines.experimental.suspendCoroutine

class InfiniteGeneratorTest {

    @Test
    fun singleton() {
        val it = generate { yield(1) }

        assert.that(it.toList(), equalTo(listOf(1)))
    }

    @Test
    fun empty() {
        val empty = generate<Any> { /**/ }

        assert.that(empty.toList(), equalTo(listOf()))
    }

    @Test
    fun many() {
        val it = generate { yield(1);yield(2) }

        assert.that(it.toList(), equalTo(listOf(1, 2)))
    }

    @Test
    fun infinity() {
        val it = generate { var it = 0; while (true) yield(it++) }

        assert.that(it.asSequence().take(10).toList(), equalTo(List(10) { it }))
    }

    @Test
    fun `yield infinity at a time`() {
        val infinity = generate { var it = 0; while (true) yield(it++) }
        val it = generate { yieldAll(infinity) }

        assert.that(it.asSequence().drop(10).take(10).toList(), equalTo(List(10) { it + 10 }))
    }

    @Test
    fun `yield empty iterators will are ignored`() {
        val it = generate { yieldAll(listOf());yield(1);yieldAll(listOf()) }

        assert.that(it.toList(), equalTo(listOf(1)))
    }

    @Test
    fun `throws exception if no remaining element when call next`() {
        val empty = generate<Any> { /**/ }

        assert.that({ empty.next() }, throws(isA<NoSuchElementException>()))
    }
}


fun <T> generate(action: suspend IteratorBuilder<T>.() -> Unit): Iterator<T> {
    return object : Iterator<T>, IteratorBuilder<T>, Continuation<Unit> {
        override val context = EmptyCoroutineContext
        private var source: Iterator<T>? = emptyIterator()
        private var step: Continuation<Unit>? = action.createCoroutine(this, this)

        override fun hasNext(): Boolean {
            while (true) {
                if (source == null) return false
                if (source!!.hasNext()) return true
                step!!.also { step = null }.resume(Unit)
            }
        }

        override fun next() = @Suppress("UNCHECKED_CAST") when {
            hasNext() -> source!!.next().also { if (!source!!.hasNext()) source = emptyIterator() }
            else -> throw NoSuchElementException()
        }

        override suspend fun yield(value: T) = yieldAll(iteratorOf(value))

        override suspend fun yieldAll(value: Iterator<T>) = run { this.source = value; suspendCoroutine<Unit> { step = it } }

        override fun resume(value: Unit) = run { source = null }

        override fun resumeWithException(exception: Throwable) = throw exception
    }
}


fun <T> iteratorOf(value: T) = object : Iterator<T> {
    private var unused = true
    override fun hasNext() = unused
    override fun next() = value.also { unused = false }
}