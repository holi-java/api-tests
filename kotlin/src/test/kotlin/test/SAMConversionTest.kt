package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import stub.TaskSet

class SAMConversionTest {
    private val tasks = TaskSet()

    @Test
    fun `convert function to java functional interface one at a time`() {
        val task: () -> Unit = { }

        assert(tasks.add(task))
        assert(tasks.add(task))

        assert(task !in tasks)
        assert.that(tasks.size(), equalTo(2))
    }

    @Test
    fun `avoid converting lambda to java functional interface more than once by using an adapter function that converts a lambda to a specific SAM type`() {
        val task = Runnable {}

        assert(tasks.add(task))
        assert(!tasks.add(task))

        assert(task in tasks)
        assert.that(tasks.size(), equalTo(1))
    }
}