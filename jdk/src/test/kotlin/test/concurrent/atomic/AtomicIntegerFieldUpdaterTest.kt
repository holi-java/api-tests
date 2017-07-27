package test.concurrent.atomic

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater

class AtomicIntegerFieldUpdaterTest {
    private @Volatile var field: Int = 0
    private val updater = AtomicIntegerFieldUpdater.newUpdater(AtomicIntegerFieldUpdaterTest::class.java, "field")


    @Test
    fun `update the field value`() {
        updater.set(this, 2)

        assert.that(field, equalTo(2))
    }


    @Test
    fun `get and accumulate`() {
        val it = updater.getAndAccumulate(this, 3) { i, j -> i - j }

        assert.that(it, equalTo(0))
        assert.that(field, equalTo(-3))
    }
}