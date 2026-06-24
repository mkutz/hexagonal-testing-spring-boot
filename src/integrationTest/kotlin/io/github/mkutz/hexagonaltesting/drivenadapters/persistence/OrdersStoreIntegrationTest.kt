package io.github.mkutz.hexagonaltesting.drivenadapters.persistence

import io.github.mkutz.hexagonaltesting.TestcontainersConfiguration
import io.github.mkutz.hexagonaltesting.application.order.Money
import io.github.mkutz.hexagonaltesting.application.order.ToStoreOrdersContract
import io.github.mkutz.hexagonaltesting.application.order.anOrderOf
import io.github.mkutz.hexagonaltesting.application.order.port.ToStoreOrders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Runs the shared [ToStoreOrdersContract] against the real `OrdersStore` JPA adapter on a real
 * Postgres, plus the one bespoke check that only real infrastructure can reveal: money lands in the
 * right column in the right representation. The credit-limit boundaries are NOT re-tested here —
 * they were exhausted in the unit suite.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class OrdersStoreIntegrationTest : ToStoreOrdersContract() {

  @Autowired private lateinit var store: ToStoreOrders

  @Autowired private lateinit var jdbcTemplate: JdbcTemplate

  override fun store(): ToStoreOrders = store

  @Test
  fun `persists money as minor units in the expected column`() {
    val order = anOrderOf(Money.euros(80))
    store.save(order)

    val amountMinor =
      jdbcTemplate.queryForObject(
        "SELECT amount_minor FROM orders WHERE id = ?",
        Long::class.java,
        order.id.value,
      )

    assertThat(amountMinor).isEqualTo(8000L)
  }
}
