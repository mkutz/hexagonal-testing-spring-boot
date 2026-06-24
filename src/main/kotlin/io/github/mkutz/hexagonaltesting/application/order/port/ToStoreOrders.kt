package io.github.mkutz.hexagonaltesting.application.order.port

import io.github.mkutz.hexagonaltesting.application.order.Order
import io.github.mkutz.hexagonaltesting.application.order.OrderId

/** Output port for persisting and retrieving [Order]s. */
interface ToStoreOrders {
  fun save(order: Order)

  fun findById(id: OrderId): Order?
}
