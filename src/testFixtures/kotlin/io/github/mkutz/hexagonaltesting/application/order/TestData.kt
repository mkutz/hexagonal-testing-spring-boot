package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.adaptersdriving.web.PlaceOrderRequest
import java.math.BigDecimal
import java.util.UUID

/**
 * Test-data builders: factory functions whose defaults are valid, random instances, so every test
 * overrides only the field it actually cares about — e.g. `anOrder(amount = Money.euros(120))`.
 * Random IDs by default keep tests collision-free in the shared integration context.
 */
fun anOrder(
  id: OrderId = OrderId.random(),
  customer: CustomerId = CustomerId.random(),
  amount: Money = Money.euros(80),
) = Order(id, customer, amount)

fun anOrderOf(amount: Money) = anOrder(amount = amount)

fun aPlaceOrderCommand(
  customer: CustomerId = CustomerId.random(),
  amount: Money = Money.euros(80),
) = PlaceOrderCommand(customer, amount)

fun aPlaceOrderRequest(
  customerId: UUID = UUID.randomUUID(),
  amount: BigDecimal = BigDecimal("80.00"),
) = PlaceOrderRequest(customerId, amount)
