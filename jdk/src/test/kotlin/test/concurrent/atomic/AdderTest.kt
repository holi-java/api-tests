@file:Suppress("MemberVisibilityCanPrivate")

package test.concurrent.atomic

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.concurrent.atomic.LongAdder

class AdderTest {
    val adder = LongAdder()

    @Test
    fun add() {
        adder.run { add(1); add(2) }

        assert.that(adder.sum(), equalTo(3L))
    }

    @Test
    fun `sum and reset`() {
        adder.add(3)

        assert.that(adder.sumThenReset(), equalTo(3L))
        assert.that(adder.sum(), equalTo(0L))
    }
}