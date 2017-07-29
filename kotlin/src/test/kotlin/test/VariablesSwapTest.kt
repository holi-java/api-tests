package test

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test

class VariablesSwapTest {

    @Test
    fun `swap functions`() {
        var a = { 1 }
        var b = { 2 }

        a = b.also { b = a }

        assert.that(a(), equalTo(2))
        assert.that(b(), equalTo(1))
    }

    @Test
    fun `swap nulls`() {
        var a: Int? = 1
        var b: Int? = null

        a = b.also { b = a }

        assert.that(a, absent())
        assert.that(b, equalTo(1))
    }


}

