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

    @Test
    fun `each singleton bean has its own prototype bean`() {
        val context = spring("inject-prototype-to-singleton-bean.xml")

        val user1 by provider { context.getBean<User>("user1") }
        val user2 by provider { context.getBean<User>("user2") }

        assert.that(user1, sameInstance(user1))
        assert.that(user2, sameInstance(user2))
        assert.that(user1, !sameInstance(user2))
        assert.that(user1, equalTo(user2))
        assert.that(user1.address, !sameInstance(user2.address))
    }

    private fun scope(scope: String) = spring(scope + "-scope.xml").run { { getBean<Date>("now") } }
}

