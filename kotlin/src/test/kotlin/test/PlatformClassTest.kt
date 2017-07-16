package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.throws
import org.junit.Test
import stub.StringUtils.toNull

class PlatformClassTest {

    @Test @Suppress("UNUSED_VARIABLE")
    fun `calling platform methods possibly throws IllegalStateException`() {
        assert.that({ val value: String = toNull("") }, throws(isA<IllegalStateException>()))
        assert.that({ val value: String = toNull("foo") }, !throws(isA<IllegalStateException>()))
        assert.that({ val value: String? = toNull("") }, !throws(isA<IllegalStateException>()))
    }


}

