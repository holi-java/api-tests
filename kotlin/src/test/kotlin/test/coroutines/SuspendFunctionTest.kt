package test.coroutines

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assert
import org.junit.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.experimental.*

class SuspendFunctionTest {

    @Test
    fun `call nested suspend function`() {
        val foo = runBlocking { provide { "bar" } }

        assert.that(foo, equalTo("bar"))
    }


    @Test
    fun `call nested async suspend function`() {
        val foo = runBlocking { async { "bar" } }

        assert.that(foo, equalTo("bar"))
    }

    @Test
    fun `async suspend function didn't suspended by default`() {
        val foo = runBlocking { async { "foot" } + async { "ball" } }

        assert.that(foo, equalTo("football"))
    }

    @Test
    fun `capture nested suspend exception`() {
        val task: () -> Unit = { runBlocking { async { throw ExpectedException } } }

        assert.that(task::invoke, throws(isA<CompletionException>(has("cause", { it.cause!! }, sameInstance<Any>(ExpectedException)))))
    }

    private object ExpectedException : RuntimeException()
}


fun <T> runBlocking(block: suspend () -> T): T {
    val it = object : Continuation<T> {
        override val context = EmptyCoroutineContext
        private val lock: Lock = ReentrantLock(true)
        private var completed = false
        private val done = lock.newCondition()
        private var exception: Throwable? = null
        private var result: T? = null

        @Suppress("UNCHECKED_CAST")
        val value: T get() = lock {
            while (!completed) done.await()
            return exception?.let { throw it } ?: result as T
        }

        override fun resume(value: T) = complete { this.result = value }

        override fun resumeWithException(exception: Throwable) = complete { this.exception = exception }

        private inline fun complete(block: () -> Unit) = lock {
            block()
            completed = true
            done.signal()
        }

        private inline fun <T> lock(block: () -> T) = lock.lock().let {
            try {
                block()
            } finally {
                lock.unlock()
            }
        }

    }
    return it.also { block.startCoroutine(it) }.value
}

suspend fun <T> async(block: () -> T): T = future(block).await()


fun <T> future(block: () -> T): CompletionStage<T> = CompletableFuture.supplyAsync(block)

suspend fun <T> CompletionStage<T>.await() = suspendCoroutine<T> { c ->
    whenComplete { value, exception ->
        when (exception) {
            null -> c.resume(value)
            else -> c.resumeWithException(exception)
        }
    }
}

suspend fun <T> provide(block: () -> T): T = suspendCoroutine { c ->
    c.resume(block())
}