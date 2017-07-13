@file:Suppress("MemberVisibilityCanPrivate")

package test.httpservers

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.sun.net.httpserver.HttpServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URL

private const val SERVER_PORT = 12345
private const val HOST_NAME = "127.0.0.1"

class HttpServerTest {

    val server = HttpServer.create(InetSocketAddress(HOST_NAME, SERVER_PORT), 0).apply {
        createContext("/").setHandler {
            try {
                it.sendResponseHeaders(200, 0)
            } finally {
                it.close()
            }
        }
    }

    @Before
    fun start() = server.start()

    @Test
    fun ping() = assert.that(get("http://${HOST_NAME}:${SERVER_PORT}") { status }, equalTo(200))

    @After
    fun stop() = server.stop(0)
}

inline fun <T> get(url: String, timeout: Int = 1000, block: HttpURLConnection.() -> T): T? {
    val connection = URL(url).openConnection().apply {
        connectTimeout = timeout
        readTimeout = timeout
    }
    with(connection as HttpURLConnection) {

        try {
            return block()
        } finally {
            disconnect()
        }
    }
}

inline val HttpURLConnection.status get() = this.responseCode