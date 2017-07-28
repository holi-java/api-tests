package test.text

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.text.Collator
import java.text.Collator.*

class CollatorTest {
    private val collator = getInstance()

    @Test
    fun `primary strength`() {
        collator.strength = PRIMARY

        assert.that(collator.compare("a", "b"), equalTo(-1))
        assert.that(collator.compare("a", "A"), equalTo(0))
        assert.that(collator.compare("A", "a"), equalTo(0))
        assert.that(collator.compare(String("a".toCharArray()), String("a".toCharArray())), equalTo(0))
        assert.that(collator.compare("ě", "e"), equalTo(0))
        assert.that(collator.compare("\u0001", "\u0002"), equalTo(0))
    }

    @Test
    fun `secondary strength`() {
        collator.strength = SECONDARY

        assert.that(collator.compare("a", "b"), equalTo(-1))
        assert.that(collator.compare("a", "A"), equalTo(0))
        assert.that(collator.compare("A", "a"), equalTo(0))
        assert.that(collator.compare(String("a".toCharArray()), String("a".toCharArray())), equalTo(0))
        assert.that(collator.compare("ě", "e"), equalTo(1))
        assert.that(collator.compare("\u0001", "\u0002"), equalTo(0))
    }

    @Test
    fun `tertiary strength`() {
        collator.strength = TERTIARY

        assert.that(collator.compare("a", "b"), equalTo(-1))
        assert.that(collator.compare("a", "A"), equalTo(-1))
        assert.that(collator.compare("A", "a"), equalTo(1))
        assert.that(collator.compare(String("a".toCharArray()), String("a".toCharArray())), equalTo(0))
        assert.that(collator.compare("ě", "e"), equalTo(1))
        assert.that(collator.compare("\u0001", "\u0002"), equalTo(0))
    }


    @Test
    fun `identical strength`() {
        collator.strength = IDENTICAL

        assert.that(collator.compare("a", "b"), equalTo(-1))
        assert.that(collator.compare("a", "A"), equalTo(-1))
        assert.that(collator.compare("A", "a"), equalTo(1))
        assert.that(collator.compare(String("a".toCharArray()), String("a".toCharArray())), equalTo(0))
        assert.that(collator.compare("ě", "e"), equalTo(1))
        assert.that(collator.compare("\u0001", "\u0002"), equalTo(-1))
    }
}