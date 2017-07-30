package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.sameInstance
import org.junit.Test
import java.util.*

class BeanScopeTest {
    @Test
    fun `singleton scope by default`() {
        val now = spring("singleton-scope.xml").run { { getBean<Date>("now") } }

        assert.that(now(), sameInstance(now()))
    }
}