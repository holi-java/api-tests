package test.text

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.text.BreakIterator
import java.text.CharacterIterator
import java.text.BreakIterator.*

private const val LINE_SEP = "\r\n"

class BreakIteratorTest {

    private val sentence = "break iterator.${LINE_SEP}example."
    @Test
    fun `break words`() {
        assert.that(testWith(getWordInstance()).toList(), equalTo(listOf("break", " ", "iterator", ".", LINE_SEP, "example", ".")))
    }

    @Test
    fun `break sentence`() {
        assert.that(testWith(getSentenceInstance()).toList(), equalTo(listOf("break iterator.$LINE_SEP", "example.")))
    }

    @Test
    fun `break lines`() {
        assert.that(testWith(getLineInstance()).toList(), equalTo(listOf("break ", "iterator.$LINE_SEP", "example.")))
    }

    private fun testWith(boundary: BreakIterator) = boundary.also { it.setText(sentence) }
}

fun BreakIterator.toList(): List<String> {
    val result = mutableListOf<String>()

    var start = first()
    while (true) {
        val next = next()
        if (next == DONE) {
            if (!isBoundary(start)) {
                result.add(text.substring(start))
            }
            break
        }
        result.add(text.substring(start, next))
        start = next
    }
    return result
}

fun CharacterIterator.substring(start: Int, end: Int? = null): String {
    val last = end ?: endIndex
    return CharArray(last - start).also {
        val pos = index
        index = start

        for (i in 0 until last - start) {
            it[i] = current().also { next() }
        }
        index = pos
    }.let { String(it) }
}