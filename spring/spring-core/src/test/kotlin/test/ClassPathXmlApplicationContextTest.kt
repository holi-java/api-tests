@file:Suppress("MemberVisibilityCanPrivate")

package test

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assert
import org.junit.Test
import org.springframework.beans.NullValueInNestedPathException
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.BeanCurrentlyInCreationException
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

    @Test
    fun `factory method`() {
        val context = ClassPathXmlApplicationContext("factory-method.xml")

        val bar = context.getBean("bar")

        assert.that(bar, equalTo<Any>("bar"))
    }

    @Test
    fun `fails on circular-dependency injection`() {
        val loadBeanDefinition: () -> Unit = { ClassPathXmlApplicationContext("circular-dependency.xml") }

        assert.that(loadBeanDefinition, throws(isA<BeanCreationException>(has("cause", { it.cause!! }, isA<BeanCurrentlyInCreationException>()))))
    }

    @Test
    fun `property namespace`() {
        val context = ClassPathXmlApplicationContext("p-namespace.xml")

        assert.that(context.getBean("date"), equalTo<Any>(Date(123)))
    }

    @Test
    fun `properties`() {
        val context = ClassPathXmlApplicationContext("properties.xml")

        assert.that(context.getBean("config"), equalTo<Any>(Optional.of(mapOf("foo" to "bar"))))
    }

    @Test
    fun `idref reference the spring bean's string ID`() {
        val context = ClassPathXmlApplicationContext("idref.xml")

        assert.that(context.getBean("foo"), equalTo<Any>("bar"))
    }

    @Test
    fun `merge collections`() {
        val context = ClassPathXmlApplicationContext("merge-collections.xml")

        assert.that(context.getBean("parent"), equalTo<Any>(Flags(listOf(1))))
        assert.that(context.getBean("child"), equalTo<Any>(Flags(listOf(1, 2))))
    }

    @Test
    fun `NULLs`() {
        val context = ClassPathXmlApplicationContext("nulls.xml")

        assert.that(context.getBean("foo"), equalTo<Any>(Optional.empty<Any>()))
    }

    @Test
    fun `constructor namespace`() {
        val context = ClassPathXmlApplicationContext("c-namespace.xml")

        assert.that(context.getBean("foo"), equalTo<Any>(Optional.of("bar")))
        assert.that(context.getBean("flags"), equalTo<Any>(Flags(listOf(1, 2, 3))))
    }

    @Test
    fun `compound property names`() {
        val context = ClassPathXmlApplicationContext("compound-property-names.xml")

        assert.that({ context.getBean("bad") }, throws(isA<BeanCreationException>(has("cause", { it.cause!! }, isA<NullValueInNestedPathException>()))))
        assert.that(context.getBean("user"), equalTo<Any>(User(Address("China"))))
    }
}

data class Flags(var flags: List<Int> = emptyList())

data class User(var address: Address? = Address())
data class Address(var country: String = "USA")