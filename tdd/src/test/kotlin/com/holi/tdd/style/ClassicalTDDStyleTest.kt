@file:Suppress("MemberVisibilityCanPrivate")

package com.holi.tdd.style

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.throws
import org.junit.Test

private const val IPHONE = "iPhone"
private const val M3 = "Mi 3"
private val WAREHOUSE get() = Warehouse().apply { add(IPHONE, 5); add(M3, 5) }

class OrderTest {
    val warehouse = WAREHOUSE

    @Test
    fun `fill order & remove inventory if there is enough items in warehouse`() {
        val inventory = warehouse[IPHONE]
        val order = Order(IPHONE, inventory)
        assert(!order.filled)

        order.fill(warehouse)

        assert(order.filled)
        assert.that(warehouse[IPHONE], equalTo(0))
    }

    @Test
    fun `doesn't fill order & remove inventory if there is no enough items in warehouse`() {
        val inventory = warehouse[IPHONE]
        val order = Order(IPHONE, inventory + 1)

        order.fill(warehouse)

        assert(!order.filled)
        assert.that(warehouse[IPHONE], equalTo(inventory))
    }
}

class WarehouseTest {
    val warehouse = WAREHOUSE

    @Test
    fun `remove inventory separately`() {
        val iphone = warehouse[IPHONE]
        val m3 = warehouse[M3]

        warehouse.remove(IPHONE, iphone)

        assert.that(warehouse[IPHONE], equalTo(0))
        assert.that(warehouse[M3], equalTo(m3))
    }

    @Test
    fun `has enough inventory for items`() {
        assert(warehouse.hasEnough(IPHONE, warehouse[IPHONE]))
        assert(!warehouse.hasEnough(IPHONE, warehouse[IPHONE] + 1))
        assert(!warehouse.hasEnough("unknown", 1))
    }

    @Test
    fun `throws illegal argument exception when remove inventory for an unknown item`() {
        assert.that({ warehouse.remove("unknown", 0) }, throws(isA<IllegalArgumentException>()))
    }

    @Test
    fun `throws illegal argument exception when remove negative inventory for an item`() {
        assert.that({ warehouse.remove(M3, -1) }, throws(isA<IllegalArgumentException>()))
    }

    @Test
    fun `throws illegal state exception if has no enough inventory when remove inventory for an item`() {
        val outOfStock = warehouse[M3] + 1

        assert.that({ warehouse.remove(M3, outOfStock) }, throws(isA<IllegalStateException>()))
    }
}