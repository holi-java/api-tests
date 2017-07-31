package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.springframework.beans.factory.InitializingBean
import javax.annotation.PostConstruct

class BeanLifecycleCallbacksTest {

    @Test
    fun `use custom callbacks instead & custom callbacks is called after afterPropertiesSet`() {
        val context = spring("custom-callbacks.xml")

        val it = context.getBean<LifecycleBean>("bean")

        assert.that(it.initializers, equalTo(listOf("afterPropertiesSet", "init")))
    }

    @Test
    fun `enable jsr-250 lifecycle annotations & jsr annotation is called before afterPropertiesSet`() {
        val context = spring("jsr250-annotation-callbacks.xml")

        val it = context.getBean<LifecycleBean>("bean")

        assert.that(it.initializers, equalTo(listOf("@PostConstruct", "afterPropertiesSet")))
    }

    @Test
    fun `enable default init-method`() {
        val context = spring("default-init-method.xml")

        val it = context.getBean<LifecycleBean>("bean")

        assert.that(it.initializers, equalTo(listOf("afterPropertiesSet", "init")))
    }
}

open class LifecycleBean : InitializingBean {
    val initializers = arrayListOf<String>()

    open fun init() = initializingBy("init")

    @PostConstruct open fun postCreated() = initializingBy("@PostConstruct")

    override fun afterPropertiesSet() = initializingBy("afterPropertiesSet")

    private fun initializingBy(initializer: String) = run { initializers += initializer }
}