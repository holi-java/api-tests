@file:Suppress("SpringKotlinAutowiring")

package test

import org.junit.Test
import org.springframework.stereotype.Component


class SpringAllOpenPluginTest {
    @Test fun `all spring components were opened`() {
        assert(SpringComponents::class.isOpen)
    }

    @Test fun `all spring component members were opened`() {
        assert(SpringComponents::index.isOpen)
    }

    @Test fun `all POJOs is still final`() {
        assert(Pojo::class.isFinal)
    }
}

@Component
class SpringComponents {

    fun index() = "foo"
}

class Pojo {
    var value: String = ""
}