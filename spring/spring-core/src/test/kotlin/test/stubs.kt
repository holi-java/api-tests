package test


data class Flags(var flags: List<Int> = emptyList())

data class User(var address: Address? = Address())
data class Address(var country: String = "USA")

data class DependsOn(val order: Int = next()) {
    private companion object OrderCounter {
        private var counter: Int = 0
        private fun next() = ++counter
    }
}