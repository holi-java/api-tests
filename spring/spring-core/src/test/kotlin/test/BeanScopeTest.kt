package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.sameInstance
import org.junit.Test
import java.util.*

class BeanScopeTest {
    @Test
    fun `singleton scope by default`() {
        val now = scope("singleton")

        assert.that(now(), sameInstance(now()))
    }

    @Test
    fun `prototype scope`() {
        val now = scope("prototype")

        assert.that(now(), !sameInstance(now()))
        assert.that(now(), equalTo(now()))
    }

    private fun scope(scope: String) = spring(scope + "-scope.xml").run { { getBean<Date>("now") } }
}