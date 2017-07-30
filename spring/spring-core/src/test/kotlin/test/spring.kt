package test

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun spring(xml: String): ApplicationContext = ClassPathXmlApplicationContext(xml)

inline fun <reified T : Any> ApplicationContext.getBean(name: String): T = getBean(name, T::class.java)

fun <T> provider(block: () -> T): ReadOnlyProperty<Any?, T> {
    return object : ReadOnlyProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T = block()
    }
}