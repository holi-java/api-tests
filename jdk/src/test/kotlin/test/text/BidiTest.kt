package test.text

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.text.Bidi
import java.text.Bidi.*

class BidiTest {

    @Test
    fun `represents all left-to-right`() {
        val bidi = Bidi("foo", DIRECTION_LEFT_TO_RIGHT)

        assert(bidi.isLeftToRight)
        assert(!bidi.isRightToLeft)
        assert(bidi.baseIsLeftToRight())
        assert(!bidi.isMixed)
        assert.that(bidi.length, equalTo(3))
        assert.that(bidi.runCount, equalTo(1))
    }

    @Test
    fun `represent all right-to-left`() {
        val bidi = Bidi("foo", DIRECTION_RIGHT_TO_LEFT)

        assert(!bidi.isLeftToRight)
        assert(!bidi.isRightToLeft)
        assert(!bidi.baseIsLeftToRight())
        assert(bidi.isMixed)
        assert.that(bidi.length, equalTo(3))
        assert.that(bidi.runCount, equalTo(1))
    }
}