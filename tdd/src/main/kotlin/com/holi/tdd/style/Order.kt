package com.holi.tdd.style


class Order(private val item: String, private val quantity: Int) {
    var filled = false; private set

    fun fill(warehouse: Warehouse) {
        if (warehouse.hasEnough(item, quantity)) {
            filled = true
            warehouse.remove(item, quantity)
        }
    }
}