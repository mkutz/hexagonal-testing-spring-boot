package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.application.order.port.ToStoreOrders
import java.util.concurrent.ConcurrentHashMap

/** Hand-written in-memory fake of [ToStoreOrders] for fast, infrastructure-free unit tests. */
class ToStoreOrdersInMemory : ToStoreOrders {

  private val store = ConcurrentHashMap<OrderId, Order>()

  override fun save(order: Order) {
    store[order.id] = order
  }

  override fun findById(id: OrderId): Order? = store[id]
}
