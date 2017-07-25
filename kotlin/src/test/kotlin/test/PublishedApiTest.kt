package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import params.paramOf

class PublishedApiTest {

    @Test
    fun `build param`() {
        val value = paramOf("foo") { map { "$it-bar" } }

        assert.that(value, equalTo("foo-bar"))
    }
}