package io.github.mkutz.hexagonaltesting.application.order.port

import io.github.mkutz.hexagonaltesting.application.order.Order
import io.github.mkutz.hexagonaltesting.application.order.OrderId

/** Input port: the seam a driving adapter uses to look up an order. */
fun interface ToGetOrder {
  fun byId(id: OrderId): Order?
}
