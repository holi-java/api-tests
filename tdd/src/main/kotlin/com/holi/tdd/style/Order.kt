package com.holi.tdd.style


class Order(private val item: String, private val quantity: Int) {
    var filled = false; private set

    fun fill(inventory: Inventory) = inventory.run { if (hasEnough(item, quantity)) remove(item, quantity).also { filled = true } }

}