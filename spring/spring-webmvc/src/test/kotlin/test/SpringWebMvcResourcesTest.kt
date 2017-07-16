@file:Suppress("MemberVisibilityCanPrivate", "HasPlatformType")

package test

import com.gargoylesoftware.htmlunit.BrowserVersion.CHROME
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class SpringWebMvcResourcesTest(descriptor: String) {

    companion object {
        @JvmStatic @Parameters(name = "{0}")
        fun `spring web xml descriptors`(): Collection<String> = listOf("/WEB-INF/web-metadata.xml", "WEB-INF/web-registration.xml")
    }

    val serverPort = 9988
    val server: Server = Server(serverPort).apply {
        stopAtShutdown = true
        handler = WebAppContext("src/main/webapp", "/").apply {
            this.descriptor = "$war/$descriptor"
        }
    }

    val client = WebClient(CHROME)

    @Before fun startWebApplication() = server.start()


    @Test
    fun `default servlet handler is enabled`() {
        val content = get("index.html")

        assert.that(content, equalTo("test"))
    }

    @Test
    fun `resources is available`() {
        val content = get("resources/index.html")

        assert.that(content, equalTo("resource"))
    }


    @After fun stopWebApplication() = server.stop()

    fun get(uri: String) = client.getPage<HtmlPage>("http://localhost:$serverPort/$uri").body.textContent
}
