package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.application.order.port.PaymentGateway
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory fake of [PaymentGateway]. Credit limits are seeded via [setCreditLimit]; charges are
 * recorded so state-based tests can assert on the resulting state rather than on method calls.
 */
class InMemoryPaymentGateway : PaymentGateway {

  private val creditLimits = ConcurrentHashMap<CustomerId, Money>()
  private val charges = ConcurrentHashMap<CustomerId, MutableList<Money>>()

  fun setCreditLimit(customer: CustomerId, limit: Money) {
    creditLimits[customer] = limit
  }

  fun chargesFor(customer: CustomerId): List<Money> = charges[customer].orEmpty()

  override fun creditLimit(customer: CustomerId): Money = creditLimits[customer] ?: Money.euros(0)

  override fun charge(customer: CustomerId, amount: Money) {
    charges.getOrPut(customer) { mutableListOf() }.add(amount)
  }
}
