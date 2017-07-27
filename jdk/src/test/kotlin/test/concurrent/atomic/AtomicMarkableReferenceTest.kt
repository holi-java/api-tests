@file:Suppress("MemberVisibilityCanPrivate")

package test.concurrent.atomic

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.util.concurrent.atomic.AtomicMarkableReference

class AtomicMarkableReferenceTest {
    val ref = AtomicMarkableReference("foo", false)


    @Test
    fun `initial value`() {
        assert(!ref.isMarked)
        assert.that(ref.reference, equalTo("foo"))
    }

    @Test
    fun mark() {
        assert(!ref.attemptMark("bar", true))
        assert(ref.attemptMark("foo", true))

        assert(ref.isMarked)
        assert.that(ref.reference, equalTo("foo"))

        assert(ref.attemptMark("foo", false))
        assert(!ref.isMarked)
    }

    @Test
    fun reset() {
        ref.set("bar", true)

        assert(ref.isMarked)
        assert.that(ref.reference, equalTo("bar"))
    }

    @Test
    fun `get reference and the mark`() {
        val mark = BooleanArray(1)
        ref.set("bar", true)

        val value = ref.get(mark)

        assert.that(value, equalTo("bar"))
        assert(mark[0])
    }
}
