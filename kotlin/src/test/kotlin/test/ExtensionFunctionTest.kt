package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test

class ExtensionFunctionTest {

    @Test
    fun `resolve extension methods by type statically`() {
        val one: Int = 2
        val number: Number = one

        fun Int.stringify() = "$this"
        fun Number.stringify() = "${toDouble()}"

        assert.that(one.stringify(), equalTo("2"))
        assert.that(number.stringify(), equalTo("2.0"))
    }
}