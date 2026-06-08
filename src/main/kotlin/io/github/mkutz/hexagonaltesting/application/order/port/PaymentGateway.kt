package io.github.mkutz.hexagonaltesting.application.order.port

import io.github.mkutz.hexagonaltesting.application.order.CustomerId
import io.github.mkutz.hexagonaltesting.application.order.Money

/** Output port for the external payment service. */
interface PaymentGateway {
  fun creditLimit(customer: CustomerId): Money

  fun charge(customer: CustomerId, amount: Money)
}
