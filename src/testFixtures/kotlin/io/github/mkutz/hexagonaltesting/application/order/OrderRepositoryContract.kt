package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.application.order.port.OrderRepository
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * The behaviour every [OrderRepository] implementation must satisfy. The in-memory fake and the JPA
 * adapter run the *same* contract, which is the mechanism that keeps the fake honest: if they
 * disagree, one of the two suites goes red.
 */
abstract class OrderRepositoryContract {

  protected abstract fun repository(): OrderRepository

  @Test
  fun `saved order can be retrieved by id`() {
    val order = anOrder()
    repository().save(order)
    assertThat(repository().findById(order.id)).isEqualTo(order)
  }

  @Test
  fun `unknown id returns null`() {
    assertThat(repository().findById(OrderId(UUID.randomUUID()))).isNull()
  }
}
