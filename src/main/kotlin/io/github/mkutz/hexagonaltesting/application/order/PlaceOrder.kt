package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.application.order.port.OrderRepository
import io.github.mkutz.hexagonaltesting.application.order.port.PaymentGateway
import org.springframework.stereotype.Service

/** Core use case: place an order if the customer is within their credit limit. */
@Service
class PlaceOrder(private val orders: OrderRepository, private val payments: PaymentGateway) {

  fun handle(command: PlaceOrderCommand): PlaceOrderResult {
    if (command.amount > payments.creditLimit(command.customer)) {
      return OrderRejected("amount exceeds credit limit")
    }
    val order = Order(OrderId.random(), command.customer, command.amount)
    orders.save(order)
    payments.charge(command.customer, command.amount)
    return OrderPlaced(order.id)
  }
}
