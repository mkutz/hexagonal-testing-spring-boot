package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.application.order.port.ToStoreOrders
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * The behaviour every [ToStoreOrders] implementation must satisfy. The in-memory fake and the JPA
 * adapter run the *same* contract, which is the mechanism that keeps the fake honest: if they
 * disagree, one of the two suites goes red.
 */
abstract class ToStoreOrdersContract {

  protected abstract fun store(): ToStoreOrders

  @Test
  fun `saved order can be retrieved by id`() {
    val order = anOrder()
    store().save(order)
    assertThat(store().findById(order.id)).isEqualTo(order)
  }

  @Test
  fun `unknown id returns null`() {
    assertThat(store().findById(OrderId(UUID.randomUUID()))).isNull()
  }
}
