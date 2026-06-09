package io.github.mkutz.hexagonaltesting.application.order.port

import io.github.mkutz.hexagonaltesting.application.order.PlaceOrderCommand
import io.github.mkutz.hexagonaltesting.application.order.PlaceOrderResult

/** Input port: the seam a driving adapter uses to place an order. */
interface ToPlaceOrder {
  fun handle(command: PlaceOrderCommand): PlaceOrderResult
}
