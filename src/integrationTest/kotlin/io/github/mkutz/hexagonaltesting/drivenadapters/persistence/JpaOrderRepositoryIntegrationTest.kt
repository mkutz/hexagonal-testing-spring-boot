package io.github.mkutz.hexagonaltesting.drivenadapters.persistence

import io.github.mkutz.hexagonaltesting.TestcontainersConfiguration
import io.github.mkutz.hexagonaltesting.application.order.Money
import io.github.mkutz.hexagonaltesting.application.order.OrderRepositoryContract
import io.github.mkutz.hexagonaltesting.application.order.anOrderOf
import io.github.mkutz.hexagonaltesting.application.order.port.OrderRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Runs the shared [OrderRepositoryContract] against the real JPA adapter on a real Postgres, plus
 * the one bespoke check that only real infrastructure can reveal: money lands in the right column
 * in the right representation. The credit-limit boundaries are NOT re-tested here — they were
 * exhausted in the unit suite.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class JpaOrderRepositoryIntegrationTest : OrderRepositoryContract() {

  @Autowired private lateinit var repository: OrderRepository

  @Autowired private lateinit var jdbcTemplate: JdbcTemplate

  override fun repository(): OrderRepository = repository

  @Test
  fun `persists money as minor units in the expected column`() {
    val order = anOrderOf(Money.euros(80))
    repository.save(order)

    val amountMinor =
      jdbcTemplate.queryForObject(
        "SELECT amount_minor FROM orders WHERE id = ?",
        Long::class.java,
        order.id.value,
      )

    assertThat(amountMinor).isEqualTo(8000L)
  }
}
