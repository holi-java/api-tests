@file:Suppress("MemberVisibilityCanPrivate")

package test

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assert
import org.junit.Test
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.context.support.GenericApplicationContext
import java.util.*

class ClassPathXmlApplicationContextTest {

    @Test
    fun standalone() {
        val context = ClassPathXmlApplicationContext("standalone.xml")

        val foo = context.getBean("foo", Optional::class.java)

        assert.that(foo.get(), equalTo<Any>("bar"))
    }

    @Test
    fun `composing XML-based metadata configuration`() {
        val context = ClassPathXmlApplicationContext("composing-xml-based-config.xml")

        val foo = context.getBean("foo", Optional::class.java)

        assert.that(foo.get(), equalTo<Any>("composed"))
    }

    @Test
    fun `lazy loading XML-based metadata definitions`() {
        val context = GenericApplicationContext()

        context.apply {
            XmlBeanDefinitionReader(this).loadBeanDefinitions("standalone.xml")
            refresh()
        }

        val foo = context.getBean("foo", Optional::class.java)
        assert.that(foo.get(), equalTo<Any>("bar"))
    }

    @Test
    fun `defines bean with multiple names`() {
        val context = ClassPathXmlApplicationContext("bean-aliasing.xml")

        val value = context.getBean("identifier", String::class.java)

        assert.that(value, equalTo("string"))
        assert.that(context.getBean("foo"), sameInstance<Any>(value))
        assert.that(context.getBean("bar"), sameInstance<Any>(value))
    }


    @Test
    fun `can't load bean without name`() {
        val context = ClassPathXmlApplicationContext("bean-without-name.xml")

        assert.that({ context.getBean("string", String::class.java) }, throws(isA<NoSuchBeanDefinitionException>()))
    }


    @Test
    fun `aliasing bean out-of bean definition`() {
        val context = ClassPathXmlApplicationContext("bean-aliasing-out-of-definition.xml")

        val value = context.getBean("identifier", String::class.java)

        assert.that(value, equalTo("string"))
        assert.that(context.getBean("foo"), sameInstance<Any>(value))
    }

    @Test
    fun `static factory method`() {
        val context = ClassPathXmlApplicationContext("static-factory-method.xml")

        val foo = context.getBean("foo", Optional::class.java)

        assert.that(foo.get(), equalTo<Any>("bar"))
    }
}
