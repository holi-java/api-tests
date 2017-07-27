package test.concurrent.atomic

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.concurrent.atomic.DoubleAccumulator

class DoubleAccumulatorTest {

    private val accumulator = DoubleAccumulator({ left, right -> left - right }, 0.0)

    @Test
    fun `initial value`() {
        assert.that(accumulator.get(), equalTo(0.0))
    }

    @Test
    fun accumulate() {
        accumulator.accumulate(3.0)
        assert.that(accumulator.get(), equalTo(-3.0))

        accumulator.accumulate(-5.0)
        assert.that(accumulator.get(), equalTo(2.0))
    }

    @Test
    fun `get and then reset`() {
        accumulator.accumulate(3.0)

        assert.that(accumulator.thenReset, equalTo(-3.0))
        assert.that(accumulator.get(), equalTo(0.0))
    }
}