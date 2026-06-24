package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.application.order.port.ToHandlePayments
import io.github.mkutz.hexagonaltesting.application.order.port.ToPlaceOrder
import io.github.mkutz.hexagonaltesting.application.order.port.ToStoreOrders
import org.springframework.stereotype.Service

/** Core use case: place an order if the customer is within their credit limit. */
@Service
class PlaceOrder(
  private val orderStore: ToStoreOrders,
  private val paymentHandler: ToHandlePayments,
) : ToPlaceOrder {

  override fun handle(command: PlaceOrderCommand): PlaceOrderResult {
    if (command.amount > paymentHandler.creditLimit(command.customer)) {
      return OrderRejected("amount exceeds credit limit")
    }
    val order = Order(OrderId.random(), command.customer, command.amount)
    orderStore.save(order)
    paymentHandler.charge(command.customer, command.amount)
    return OrderPlaced(order.id)
  }
}
