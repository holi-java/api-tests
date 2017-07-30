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
import org.springframework.context.support.GenericApplicationContext
import java.util.*

class ClassPathXmlApplicationContextTest {

    @Test
    fun standalone() {
        val context = spring("standalone.xml")

        assert.that(context.getBean<Optional<String>>("foo").get(), equalTo("bar"))
    }

    @Test
    fun `composing XML-based metadata configuration`() {
        val context = spring("composing-xml-based-config.xml")

        assert.that(context.getBean<Optional<String>>("foo").get(), equalTo("composed"))
    }

    @Test
    fun `lazy loading XML-based metadata definitions`() {
        val context = GenericApplicationContext()

        context.apply {
            XmlBeanDefinitionReader(this).loadBeanDefinitions("standalone.xml")
            refresh()
        }

        assert.that(context.getBean<Optional<String>>("foo").get(), equalTo("bar"))
    }

    @Test
    fun `defines bean with multiple names`() {
        val context = spring("bean-aliasing.xml")

        val value = context.getBean<String>("identifier")

        assert.that(value, equalTo("string"))
        assert.that(context.getBean("foo"), sameInstance<Any>(value))
        assert.that(context.getBean("bar"), sameInstance<Any>(value))
    }


    @Test
    fun `can't load bean without name`() {
        val context = spring("bean-without-name.xml")

        assert.that({ context.getBean("string") }, throws(isA<NoSuchBeanDefinitionException>()))
    }


    @Test
    fun `aliasing bean out-of bean definition`() {
        val context = spring("bean-aliasing-out-of-definition.xml")

        val value = context.getBean<String>("identifier")

        assert.that(value, equalTo("string"))
        assert.that(context.getBean("foo"), sameInstance<Any>(value))
    }

    @Test
    fun `static factory method`() {
        val context = spring("static-factory-method.xml")

        val foo = context.getBean<Optional<String>>("foo")

        assert.that(foo.get(), equalTo("bar"))
    }

    @Test
    fun `factory method`() {
        val context = spring("factory-method.xml")

        val bar = context.getBean<String>("bar")

        assert.that(bar, equalTo("bar"))
    }

    @Test
    fun `fails on circular-dependency injection`() {
        val loadBeanDefinition: () -> Unit = { spring("circular-dependency.xml") }

        assert.that(loadBeanDefinition, throws(isA<BeanCreationException>(has("cause", { it.cause!! }, isA<BeanCurrentlyInCreationException>()))))
    }

    @Test
    fun `property namespace`() {
        val context = spring("p-namespace.xml")

        assert.that(context.getBean<Date>("date").time, equalTo(123L))
    }

    @Test
    fun `properties`() {
        val context = spring("properties.xml")

        assert.that(context.getBean<Optional<Map<String, String>>>("config"), equalTo(Optional.of(mapOf("foo" to "bar"))))
    }

    @Test
    fun `idref reference the spring bean's string ID`() {
        val context = spring("idref.xml")

        assert.that(context.getBean<String>("foo"), equalTo("bar"))
    }

    @Test
    fun `merge collections`() {
        val context = spring("merge-collections.xml")

        assert.that(context.getBean<Flags>("parent"), equalTo(Flags(listOf(1))))
        assert.that(context.getBean<Flags>("child"), equalTo(Flags(listOf(1, 2))))
    }

    @Test
    fun `NULLs`() {
        val context = spring("nulls.xml")

        assert.that(context.getBean<Optional<Any>>("foo"), equalTo(Optional.empty<Any>()))
    }

    @Test
    fun `constructor namespace`() {
        val context = spring("c-namespace.xml")

        assert.that(context.getBean<Optional<String>>("foo"), equalTo(Optional.of("bar")))
        assert.that(context.getBean<Flags>("flags"), equalTo(Flags(listOf(1, 2, 3))))
    }

    @Test
    fun `compound property names`() {
        val context = spring("compound-property-names.xml")

        assert.that({ context.getBean("bad") }, throws(isA<BeanCreationException>(has("cause", { it.cause!! }, isA<NullValueInNestedPathException>()))))
        assert.that(context.getBean<User>("user"), equalTo(User(Address("China"))))
    }

    @Test
    fun `depends-on attribute`() {
        val context = spring("depends-on-attribute.xml")

        assert.that(context.getBean<DependsOn>("first"), equalTo(DependsOn(1)))
        assert.that(context.getBean<DependsOn>("second"), equalTo(DependsOn(2)))
    }
}

data class Flags(var flags: List<Int> = emptyList())

data class User(var address: Address? = Address())
data class Address(var country: String = "USA")

data class DependsOn(val order: Int = next()) {
    private companion object OrderCounter {
        private var counter: Int = 0
        private fun next() = ++counter
    }
}