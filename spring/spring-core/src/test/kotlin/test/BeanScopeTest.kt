package test

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assert
import org.junit.Test
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.config.Scope
import org.springframework.context.support.SimpleThreadScope
import java.util.*

class BeanScopeTest {
    @Test
    fun `singleton scope by default`() {
        val date = scope("singleton")

        assert.that(date(), sameInstance(date()))
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

    @Test
    fun `make sure singleton bean return a new prototype bean each time by aop scoped-proxy`() {
        val context = spring("inject-aop-proxy-prototype-to-singleton-bean.xml")

        val user by provider { context.getBean<User>("user") }

        assert.that(user, sameInstance(user))
        assert.that(user.address!!.country, !sameInstance(user.address!!.country))
        assert.that(user.address!!.country, equalTo(user.address!!.country))
    }

    @Test
    fun `register custom scope programmatically`() {
        val context = spring("thread-scope-bean.xml")

        assert.that({ context.getBean<String>("foo") }, throws(isA<IllegalStateException>(has("message", { it.message!! }, containsSubstring("scope name 'thread'")))))

        with(context.autowireCapableBeanFactory as ConfigurableBeanFactory) { registerScope("thread", SimpleThreadScope()) }

        assert.that(context.getBean<String>("foo"), equalTo("bar"))
    }

    @Test
    fun `register custom scope in XML`() {
        val context = spring("register-thread-scope.xml")


        val scope = with(context.autowireCapableBeanFactory as ConfigurableBeanFactory) {
            getRegisteredScope("thread")
        }

        assert.that(scope, isA<SimpleThreadScope>())
    }

    private fun scope(scope: String) = spring(scope + "-scope.xml").run { { getBean<Date>("date") } }

}
