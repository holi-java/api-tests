package test.text

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.text.MessageFormat
import java.text.MessageFormat.*

class MessageFormatTest {
    @Test
    fun `format string`() {
        assert.that(format("{0}ball", "foot"), equalTo("football"))
    }

    @Test
    fun `represent as literal if the value missing`() {
        assert.that(format("{0}"), equalTo("{0}"))
    }

    @Test
    fun `format number`() {
        assert.that(format("{0,number,#,###}", 12345), equalTo("12,345"))
    }

    @Test
    fun `represent {index} if the value missing`() {
        assert.that(format("{0,number}"), equalTo("{0}"))
    }

    @Test
    fun `escape char '`() {
        assert.that(format("'{0}", "ignored"), equalTo("{0}"))
    }

    @Test
    fun `parsing strings`() {
        assert.that("foo".parse("{0}"), equalTo(listOf<Any>("foo")))
        assert.that("1234".parse("{0}"), equalTo(listOf<Any>("1234")))
    }

    @Test
    fun `parsing numbers`() {
        assert.that("1234".parse("{0,number}"), equalTo(listOf<Any>(1234L)))
        assert.that("1234.0".parse("{0,number}"), equalTo(listOf<Any>(1234L)))
        assert.that("1234.123".parse("{0,number}"), equalTo(listOf<Any>(1234.123)))
    }
}

fun String.parse(pattern: String) = MessageFormat(pattern).parse(this).toList()