package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.SmartLifecycle
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

    @Test
    fun `enable spring lifecycle bean`() {
        val context = spring("spring-lifecycle-beans.xml")

        context.start()

        assert.that(context.getBean<SpringLifecycleBean>("started").starts, equalTo(0))
        assert.that(context.getBean<SpringLifecycleBean>("new").starts, equalTo(1))

    }

    @Test
    fun `stop spring lifecycle bean`() {
        val context = spring("spring-lifecycle-beans.xml")
        val it = context.getBean<SpringLifecycleBean>("started")
        assert(!it.stoped)

        context.stop()

        assert(it.stoped)
    }
}

open class LifecycleBean : InitializingBean {
    val initializers = arrayListOf<String>()

    open fun init() = initializingBy("init")

    @PostConstruct open fun postCreated() = initializingBy("@PostConstruct")

    override fun afterPropertiesSet() = initializingBy("afterPropertiesSet")

    private fun initializingBy(initializer: String) = run { initializers += initializer }
}

class SpringLifecycleBean(private var started: Boolean) : SmartLifecycle {
    var starts: Int = 0
    @Volatile var stoped = false;
    override fun isRunning(): Boolean = started
    override fun start() = let { started = true }.also { starts++ }
    override fun stop() = run { stoped = true }

    override fun stop(callback: Runnable) = callback.run().also { stop() }

    override fun isAutoStartup() = true

    override fun getPhase() = 1
}