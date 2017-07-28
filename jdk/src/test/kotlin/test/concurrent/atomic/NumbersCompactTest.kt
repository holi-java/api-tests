package test.concurrent.atomic

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.junit.Test

class NumbersCompactTest {

    @Test
    fun empty() {
        assert.that(arrayOf<Int>().compact(), equalTo(""))
    }

    @Test
    fun singleton() {
        assert.that(arrayOf(1).compact(), equalTo("1"))
    }

    @Test
    fun pairs() {
        assert.that(arrayOf(1, 2).compact(), equalTo("1,2"))
    }

    @Test
    fun `compact if subsequent numbers more than 3`() {
        assert.that(arrayOf(1, 2, 3).compact(), equalTo("1-3"))
    }

    @Test
    fun `don't compact for non continuous numbers`() {
        assert.that(arrayOf(1, 2, 4).compact(), equalTo("1,2,4"))
    }

    @Test
    fun `mixed`() {
        assert.that(arrayOf(1, 2, 3, 4, 5, 9, 10, 11, 13, 14).compact(), equalTo("1-5,9-11,13,14"))
    }

    @Test
    fun `throws exception for bad format  numbers`() {
        assert.that({ arrayOf(1, 1).compact() }, throws<IllegalArgumentException>())
    }
}

fun Array<Int>.compact(): String {
    return StringBuilder().also { compact(it) }.toString()
}

private fun Array<Int>.compact(out: Appendable): Unit {
    validate().run {
        if (size == 0) return
        var end = get(0)
        var start = end

        for (i in 1 until size) {
            val it = get(i)
            if (end + 1 != it) {
                out.compact(start, end).append(',')
                start = it
            }
            end = it
        }

        out.compact(start, end)
    }
}

private fun Array<Int>.validate(): Unit {
    if (size < 2) return
    var prev = get(0)
    for (i in 1 until size) {
        prev = get(i).takeIf { it > prev } ?: throw IllegalArgumentException()
    }
}

private fun Appendable.compact(start: Int, end: Int) = append(start.toString()).also {
    val sep = when (end - start) {
        0 -> return this
        1 -> ','
        else -> '-'
    }
    append(sep).append(end.toString())
}