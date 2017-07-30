package test

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

fun spring(xml: String): ApplicationContext = ClassPathXmlApplicationContext(xml)

inline fun <reified T : Any> ApplicationContext.getBean(name: String): T = getBean(name, T::class.java)