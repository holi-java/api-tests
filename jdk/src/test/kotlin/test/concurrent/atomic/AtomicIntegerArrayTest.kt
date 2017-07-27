package test.concurrent.atomic

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.concurrent.atomic.AtomicIntegerArray

class AtomicIntegerArrayTest {

    private val array = AtomicIntegerArray(5)

    @Test
    fun `initial states`() {
        assert.that(array.length(), equalTo(5))
        assert.that(array.toString(), equalTo("[0, 0, 0, 0, 0]"))
    }

    @Test
    fun `lazy set`() {
        array.lazySet(0, 1)
        array.lazySet(0, 2)

        assert.that(array[0], equalTo(2))
    }

    @Test
    fun `accumulate and get`() {
        val it = array.accumulateAndGet(0, 3) { i, j -> i - j }

        assert.that(it, equalTo(-3))
        assert.that(it, equalTo(array[0]))
    }

    @Test
    fun `get and then accumulate`() {
        val it = array.getAndAccumulate(0, 3) { i, j -> i - j }

        assert.that(it, equalTo(0))
        assert.that(array[0], equalTo(-3))
    }

    @Test
    fun `compare and set`() {
        assert(!array.compareAndSet(0, -1, 2))
        assert.that(array[0], equalTo(0))

        assert(array.weakCompareAndSet(0, 0, 2))
        assert.that(array[0], equalTo(2))
    }
}