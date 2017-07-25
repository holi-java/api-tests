package params

interface Param<out T> {
    fun <R> map(action: (T) -> R): R
}

private class Singleton<out T>(private val value: T) : Param<T> {
    override fun <R> map(action: (T) -> R) = action(value)
}

inline fun <reified T, R> paramOf(value: T, block: Param<T>.() -> R): R = singleton(value).block()

@PublishedApi @JvmSynthetic internal fun <T> singleton(value: T): Param<T> = Singleton(value)