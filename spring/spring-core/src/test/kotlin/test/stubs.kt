package test


data class Flags(var flags: List<Int> = emptyList())

data class User(var address: Address? = Address())
open class Address(open var country: String = "USA") {
    override fun equals(other: Any?) = when {
        other === this -> true
        other is Address -> other.country == country
        else -> false
    }

    override fun hashCode() = country.hashCode()

    override fun toString() = country
}

data class DependsOn(val order: Int = next()) {
    private companion object OrderCounter {
        private var counter: Int = 0
        private fun next() = ++counter
    }
}