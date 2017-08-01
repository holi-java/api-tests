package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.greaterThan
import com.natpryce.hamkrest.hasSize
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool

class ArrayInitializationTest {
    @Test
    fun `create matrix`() {

        val matrix = Matrix2D(2, 2) { x, y -> (x + 1) * (y + 1) }

        assert.that(matrix.map { it.map { it } }, equalTo(listOf(
                listOf(1, 2),
                listOf(2, 4)
        )))
    }

    @Test
    fun `initializing in parallel`() {
        val threads = Matrix2D(10, 3) { _, _ -> Thread.currentThread() }.flatten().distinct()

        assert.that(threads, hasSize(greaterThan(1)))
    }

    @Test
    fun `initializing int array in parallel`() {
        val it = generatorOf(3) { it * 2 }
        assert.that(it.map { it() }, equalTo(listOf(0, 2, 4)))
    }

    @Test
    fun `convert array to another array`() {
        val it = arrayOf(1, 2, 3).toArray { it.toString() }

        assert.that(it.toList(), equalTo(listOf("1", "2", "3")))
    }
}


//@formatter:off
inline fun <reified T> Matrix2D(w: Int, h: Int, crossinline init: (Int, Int) -> T) =
        //                         v--- init array elements in parallel
        generatorOf(w) { x -> Array(h) { y -> init(x, y) } }.toArray { it() }
        //                        transform to the  `test.Matrix2D` ---^


//                                  v--- convert Array<T> to Array<R>
inline fun <T, reified R> Array<T>.toArray(transform: (T) -> R) =Array(size) { i -> transform(get(i)) }



//                     v--- create array generator
inline fun <reified T> generatorOf(size: Int, crossinline init: (Int) -> T) = ForkJoinPool(20).let {
    Array(size) { i -> it.submit(Callable { init(i) }).let { { it.get() } } }
    //                      return the lambda generator ()->T  ---^
}

//@formatter:on