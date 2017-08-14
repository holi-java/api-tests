package com.holi.tdd.style

class Warehouse {
    private var inventory = mutableMapOf<String, Int>()
    fun add(item: String, quantity: Int) = run { inventory[item] = get(item) + quantity }

    operator fun get(item: String) = inventory[item] ?: 0

    fun hasEnough(item: String, quantity: Int) = get(item) >= quantity

    fun remove(item: String, quantity: Int) = check(hasEnough(item, quantity)).let {
        inventory[item] = require(quantity > 0).let { requireNotNull(inventory[item]) - quantity }
    }
}