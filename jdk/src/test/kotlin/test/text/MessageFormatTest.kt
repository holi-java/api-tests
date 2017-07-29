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
}