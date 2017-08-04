package test.coroutines

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.throws
import org.junit.Assert.fail
import org.junit.Test
import java.util.*
import kotlin.coroutines.experimental.*

class GeneratorTest {

    @Test
    fun singleton() {
        val it = generate { yield(1) }

        assert.that(it.toList(), equalTo(listOf(1)))
    }

    @Test
    fun `many`() {
        val it = generate { yield(1); yield(2) }

        assert.that(it.toList(), equalTo(listOf(1, 2)))
    }

    @Test
    fun `yield many`() {
        val it = generate { yieldAll(listOf(1, 2)); }

        assert.that(it.toList(), equalTo(listOf(1, 2)))
    }

    @Test
    fun `yield empty first`() {
        val it = generate { yieldAll(emptyList());yield(1) }

        assert.that(it.toList(), equalTo(listOf(1)))
    }

    @Test
    fun `suspend the next subroutines`() {
        val it = generate { yield(1); fail() }

        assert.that(it.next(), equalTo(1))
        assert.that({ it.next() }, throws(isA<AssertionError>()))
    }


    @Test
    fun `throws exception if no elements remaining`() {
        val it = generate<Unit> { /**/ }.iterator()

        assert.that({ it.next() }, throws(isA<NoSuchElementException>()))
    }


    @Test
    fun `has next`() {
        val it = generate { yieldAll(listOf(1)) }
        assert(it.hasNext())
        assert(it.hasNext())

        it.next()

        assert(!it.hasNext())
    }
}

fun <T> Iterator<T>.toList() = asSequence().toList()



fun <T> generate(action: suspend IteratorBuilder<T>.() -> Unit): Iterator<T> {
    return object : Iterator<T>, IteratorBuilder<T>, Continuation<Unit> {
        override val context = EmptyCoroutineContext
        private val queue: Queue<T> = LinkedList<T>()
        private var nextStep: Continuation<Unit>? = action.createCoroutine(this, this)
        override fun hasNext(): Boolean {
            while (queue.isEmpty()) nextStep?.resume(Unit) ?: return false
            return true
        }

        @Suppress("UNCHECKED_CAST")
        override fun next() = when {
            hasNext() -> queue.poll()
            else -> throw NoSuchElementException()
        }

        suspend override fun yield(value: T) = yieldAll(listOf(value))

        suspend override fun yieldAll(value: Iterable<T>) {
            this.queue += value
            suspendCoroutine<Unit> { c ->
                nextStep = c
            }
        }

        override fun resume(value: Unit) = let { nextStep = null }

        override fun resumeWithException(exception: Throwable) = throw exception
    }

}

