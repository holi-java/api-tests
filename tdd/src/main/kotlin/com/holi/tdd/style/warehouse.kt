package com.holi.tdd.style

interface Inventory {
    operator fun get(item: String): Int
    fun hasEnough(item: String, quantity: Int): Boolean
    fun remove(item: String, quantity: Int)
}

class Warehouse : Inventory {
    private var inventory = mutableMapOf<String, Int>()
    fun add(item: String, quantity: Int) = run { inventory[item] = get(item) + quantity }

    override operator fun get(item: String) = inventory[item] ?: 0

    override fun hasEnough(item: String, quantity: Int) = get(item) >= quantity

    override fun remove(item: String, quantity: Int) = check(hasEnough(item, quantity)).let {
        inventory[item] = require(quantity > 0).let { requireNotNull(inventory[item]) - quantity }
    }
}