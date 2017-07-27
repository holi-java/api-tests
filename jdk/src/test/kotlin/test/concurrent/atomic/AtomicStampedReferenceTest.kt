package test.concurrent.atomic

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.concurrent.atomic.AtomicStampedReference

class AtomicStampedReferenceTest {
    private val ref = AtomicStampedReference("foo", 0)


    @Test
    fun `get value and the stamp`() {
        val stamp = IntArray(1)
        ref.set("bar", 3)

        val it = ref.get(stamp)

        assert.that(it, equalTo("bar"))
        assert.that(stamp.toList(), equalTo(listOf(3)))
    }
}