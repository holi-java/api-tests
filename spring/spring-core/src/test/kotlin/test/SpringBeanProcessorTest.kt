@file:Suppress("MemberVisibilityCanPrivate")

package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmpty
import com.natpryce.hamkrest.sameInstance
import org.junit.Test
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.context.support.StaticApplicationContext

class SpringBeanProcessorTest {

    val applicationContext = StaticApplicationContext()
    val beanFactory = applicationContext.defaultListableBeanFactory
    val created = arrayListOf<String>()
    val processor = object : BeanPostProcessor {
        override fun postProcessBeforeInitialization(bean: Any, beanName: String?): Any = bean.also { created += beanName!! }

        override fun postProcessAfterInitialization(bean: Any, beanName: String?): Any = bean
    }

    @Test
    fun `applicationContext's BeanFactory is-a ConfigurableBeanFactory`() {
        assert.that(beanFactory, sameInstance<Any>(applicationContext.beanFactory))
    }

    @Test
    fun `didn't register a bean processor as a bean`() {
        beanFactory.addBeanPostProcessor(processor)

        assert.that(created, isEmpty)
    }

    @Test
    fun `trigger the processor after bean created`() {
        beanFactory.addBeanPostProcessor(processor)

        applicationContext.registerBeanDefinition("foo", BeanDefinitionBuilder.rootBeanDefinition(String::class.java).addConstructorArgValue("bar").beanDefinition)
        assert.that(created, isEmpty)

        assert.that(applicationContext.getBean<String>("foo"), equalTo("bar"))
        assert.that(created, equalTo(listOf("foo")))
    }
}