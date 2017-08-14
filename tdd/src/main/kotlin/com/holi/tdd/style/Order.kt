package com.holi.tdd.style


class Order(private val item: String, private val quantity: Int) {
    var filled = false; private set

    fun fill(inventory: Inventory) {
        if (inventory.hasEnough(item, quantity)) {
            inventory.remove(item, quantity)
            filled = true
        }
    }
}