package io.github.mkutz.hexagonaltesting.application.order

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests for the core use case against in-memory fakes — no Spring, no mocking framework, no
 * infrastructure. This is where business-logic risk is exhausted: every equivalence class and
 * boundary of the credit-limit rule lives here, so the higher layers never re-test it.
 */
class PlaceOrderTest {

  private val orderStore = ToStoreOrdersInMemory()
  private val paymentHandler = ToHandlePaymentsInMemory()
  private val orderService = OrderService(orderStore, paymentHandler)

  @Test
  fun `placing an order below the credit limit succeeds`() {
    val customer = CustomerId.random()
    paymentHandler.setCreditLimit(customer, Money.euros(100))

    val result = orderService.handle(aPlaceOrderCommand(customer, Money.euros(80)))

    assertThat(result).isInstanceOf(OrderPlaced::class.java)
    assertThat(orderStore.findById((result as OrderPlaced).orderId)).isNotNull()
  }

  @Test
  fun `placing an order exactly at the credit limit succeeds`() {
    val customer = CustomerId.random()
    paymentHandler.setCreditLimit(customer, Money.euros(100))

    val result = orderService.handle(aPlaceOrderCommand(customer, Money.euros(100)))

    assertThat(result).isInstanceOf(OrderPlaced::class.java)
  }

  @Test
  fun `placing an order one cent above the credit limit is rejected`() {
    val customer = CustomerId.random()
    paymentHandler.setCreditLimit(customer, Money.euros(100))

    val result = orderService.handle(aPlaceOrderCommand(customer, Money.ofMinorUnits(10_001)))

    assertThat(result).isInstanceOf(OrderRejected::class.java)
  }

  @Test
  fun `placing an order well above the credit limit is rejected and persists nothing`() {
    val customer = CustomerId.random()
    paymentHandler.setCreditLimit(customer, Money.euros(100))

    val result = orderService.handle(aPlaceOrderCommand(customer, Money.euros(120)))

    assertThat(result).isInstanceOf(OrderRejected::class.java)
    assertThat(paymentHandler.chargesFor(customer)).isEmpty()
  }

  @Test
  fun `a successful order charges the customer exactly the order amount`() {
    val customer = CustomerId.random()
    paymentHandler.setCreditLimit(customer, Money.euros(100))

    orderService.handle(aPlaceOrderCommand(customer, Money.euros(80)))

    assertThat(paymentHandler.chargesFor(customer)).containsExactly(Money.euros(80))
  }
}
