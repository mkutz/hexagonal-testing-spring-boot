package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.application.order.port.OrderRepository
import java.util.concurrent.ConcurrentHashMap

/** Hand-written in-memory fake of [OrderRepository] for fast, infrastructure-free unit tests. */
class InMemoryOrderRepository : OrderRepository {

  private val store = ConcurrentHashMap<OrderId, Order>()

  override fun save(order: Order) {
    store[order.id] = order
  }

  override fun findById(id: OrderId): Order? = store[id]
}
