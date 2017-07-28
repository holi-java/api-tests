package test.text

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.text.CharacterIterator
import java.text.CharacterIterator.*
import java.text.StringCharacterIterator

class CharacterIteratorTest {
    private val it = StringCharacterIterator("football", 1, 5, 1)

    @Test
    fun `initial value`() {
        assert.that(it.index, equalTo(1))
        assert.that(it.first(), equalTo('o'))
        assert.that(it.last(), equalTo('b'))
        assert.that(it.beginIndex, equalTo(1))
        assert.that(it.endIndex, equalTo(5))
        assert.that(it.index, equalTo(4))
    }

    @Test
    fun `iterate chars`() {
        assert.that(it.remaining(), equalTo("ootb"))

        it.next()
        assert.that(it.remaining(), equalTo("otb"))
    }

    @Test
    fun reset() {
        it.setText("bar")

        assert.that(it.beginIndex, equalTo(0))
        assert.that(it.endIndex, equalTo(3))
        assert.that(it.remaining(), equalTo("bar"))
        assert.that(it.remaining(), equalTo("bar"))
    }
}

fun CharacterIterator.remaining(): String {
    return index.let {
        val chars = CharArray(endIndex - it)
        var i = 0
        while (current() != DONE) chars[i++] = current().also { next() }
        return@let String(chars)
    }.also { index -= it.length }
}