package test.concurrent.atomic

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.throws
import org.junit.Test
import java.util.*

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
        assert.that({ arrayOf(1, 1).compact() }, throws<IllegalArgumentException>(has("message", { it.message }, equalTo("Bad format: [1, 1]"))))
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Array<Int>.compact() = StringBuilder().also { compact(it) }.toString()

fun Array<Int>.compact(out: Appendable): Unit {
    takeIf { valid() }?.run {
        var prev = get(0)
        var start = prev

        for (i in 1 until size) prev = get(i).also {
            if (prev + 1 != it) {
                out.compact(start, prev).append(',')
                start = it
            }
        }
        out.compact(start, prev)
    }
}

private fun Array<Int>.valid(): Boolean {
    if (size == 0) return false
    var prev = get(0)
    for (i in 1 until size) prev = get(i).takeIf { it > prev } ?: throw IllegalArgumentException("Bad format: ${Arrays.toString(this)}")
    return true;
}

private fun Appendable.compact(start: Int, end: Int) = append(start.toString()).also {
    separate(end - start) { append(it).append(end.toString()) }
}

private inline fun separate(distance: Int, block: (Char) -> Unit): Unit {
    return block(when (distance) {
        0 -> return
        1 -> ','
        else -> '-'
    })
}
