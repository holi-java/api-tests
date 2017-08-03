package test.coroutines

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.sameInstance
import com.natpryce.hamkrest.throws
import org.junit.Test
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.createCoroutine

class ContinuationTest {


    @Test
    fun `a suspend function will be run when resume`() {
        var executed = false
        val it = suspend { executed = true }
        assert(!executed)

        it.resume(Unit)

        assert(executed)
    }

    @Test
    fun `can't resume a continuation more than once`() {
        val it = suspend { /**/ }

        it.resume(Unit)

        assert.that({ it.resume(Unit) }, throws(isA<IllegalStateException>()))
    }

    @Test
    fun `capture the exception occurs in suspend function`() {
        val expectedException = IllegalStateException()

        val it = suspend { throw expectedException }

        assert.that({ it.resume(Unit) }, throws(sameInstance(expectedException)))
    }

    private fun suspend(block: suspend () -> Unit): Continuation<Unit> {
        return block.createCoroutine(object : Continuation<Unit> {
            override val context = EmptyCoroutineContext

            override fun resume(value: Unit) = Unit

            override fun resumeWithException(exception: Throwable) = throw exception

        })
    }
}