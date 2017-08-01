package test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
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
        val threads = Matrix2D(1, 3) { x, y -> Thread.currentThread() }.flatten()

        assert.that(threads.size, equalTo(3))
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
inline fun <reified T> generatorOf(size: Int, crossinline init: (Int) -> T) =
        Array(size) { i ->
            ForkJoinPool.commonPool().submit(Callable { init(i) })
            //                         v--- return the lambda generator ()->T
                                     .let { { it.get() } }
        }

//@formatter:on